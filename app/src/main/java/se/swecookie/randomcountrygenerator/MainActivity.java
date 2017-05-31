package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imgCountry;
    private TextView txtCountryName;
    private Button btnRandom, btnOpen;
    private AdView mAdView;

    private List<String> countryList;

    private int delayInMillis = delayOrigin;
    private static final int delayOrigin = 20;

    private FirebaseAnalytics mFirebaseAnalytics;

    private MediaPlayer mp;

    private AlertDialog privacyBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countryList = new ArrayList<>();

        mp = MediaPlayer.create(this, R.raw.blip);

        imgCountry = (ImageView) findViewById(R.id.imgCountryFlag);
        txtCountryName = (TextView) findViewById(R.id.txtCountryName);
        btnRandom = (Button) findViewById(R.id.btnRandom);
        btnOpen = (Button) findViewById(R.id.btnOpen);
        btnOpen.setVisibility(View.GONE);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Sample AdMob app ID: ca-app-pub-xxxxxxxxxxxxxxxxxxxxxxxxxxx
        MobileAds.initialize(this, "ca-app-pub-2831297200743176~3098371641");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // För testning
        //AdRequest request = new AdRequest.Builder().addTestDevice("-").build();
        //mAdView.loadAd(request);
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnRandom:
                if (!checkIfAceptedPP()) {
                    displayPrivacyPolicyNotification();
                } else {
                    onNewCountryClicked();
                }
                break;
            case R.id.btnOpen:
                if (checkConnection()) {
                    sendToFirebase("Clicked Open");
                    Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + txtCountryName.getText().toString().replace(" ", "+"));
                    Log.e("uri", uri.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    showConnectionError();
                }
                break;
            case R.id.txtAbout:
                showAbout();
                break;
        }
    }

    private void onNewCountryClicked() {
        btnRandom.setEnabled(false);
        btnOpen.setEnabled(false);
        startLoop();
        sendToFirebase("Clicked Random");
    }

    private boolean checkIfAceptedPP() {
        SharedPreferences prefs = getSharedPreferences("accepted", MODE_PRIVATE);
        return prefs.getBoolean("acceptedPP", false);
    }

    private void setAcceptedPP(boolean accepted) {
        SharedPreferences.Editor editor = getSharedPreferences("accepted", MODE_PRIVATE).edit();
        editor.putBoolean("acceptedPP", accepted);
        editor.apply();
        if (accepted) {
            onNewCountryClicked();
        }
    }

    private void displayPrivacyPolicyNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Privacy Policy");
        builder.setMessage(getString(R.string.main_privacy_policy_message));
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setAcceptedPP(true);
            }
        });
        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setAcceptedPP(false);
                finish();
            }
        });
        builder.setNeutralButton("Read it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String privacyPolicy = "https://www.swecookie.se/apps/privacy-policies/RCG-PP.pdf";
                final Uri uri = Uri.parse("http://docs.google.com/gview?embedded=true&url=" + privacyPolicy);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        privacyBuilder = builder.create();
        privacyBuilder.show();
    }

    /**
     * Ta bort test ads och lägg till riktiga innan lansering (i activity_main.xml)
     */

    private void sendToFirebase(String s) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, s);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("About");
        builder.setIcon(R.drawable.se);
        builder.setMessage(getString(R.string.main_about_message));
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void showConnectionError() { // If no connection
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Device offline");
        builder.setMessage("Internet connection required! Please enable it and retry.");
        builder.setPositiveButton("Ok, I'll turn it on!", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startLoop() {
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                mp.start();
                randomCountry();
                if (delayInMillis < 150) {
                    delayInMillis += 5;
                } else if (delayInMillis < 400) {
                    delayInMillis += 80;
                } else if (delayInMillis < 800) {
                    delayInMillis += 130;
                }
                if (delayInMillis >= 800) {
                    onLoopStopped();
                    ha.removeCallbacks(this);
                } else {
                    ha.postDelayed(this, delayInMillis);
                }
            }
        }, delayInMillis);
    }

    private void onLoopStopped() {
        delayInMillis = delayOrigin;
        btnRandom.setEnabled(true);
        btnOpen.setVisibility(View.VISIBLE);
        btnOpen.setEnabled(true);
    }

    private void randomCountry() {
        if (countryList.size() == 0) {
            String c = null;
            try {
                c = getCountriesAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert c != null;
            String[] countries = c.split("\n");

            Collections.addAll(countryList, countries);
            // Delete first and last "<countries>"
            countryList.remove(countryList.size() - 1);
            countryList.remove(0);
        }

        showRandomCountry();
    }

    private void showRandomCountry() {
        int random = (int) (Math.random() * (countryList.size() - 1));

        final String country = countryList.get(random);
        String s = country.split(">")[1];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '<') {
                sb.append(s.charAt(i));
            } else {
                break;
            }
        }

        final String countryName = sb.toString();
        final String countryCode = country.split("\"")[1];

        setLayout(countryName, countryCode);
    }

    private void setLayout(String countryName, String countryCode) {
        // if country code = "do", get file do1.png (reserved java keyword)
        if (countryCode.equals("DO")) {
            countryCode = "do1";
        }
        txtCountryName.setText(countryName);
        try {
            imgCountry.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, getResources().
                    getIdentifier(countryCode.toLowerCase(), "drawable", "se.swecookie.randomcountrygenerator")));
        } catch (Resources.NotFoundException e) {
            // Set default image
            Log.e("error", "Couldn't find " + countryName);
            imgCountry.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.mipmap.ikon));
        }

    }

    /**
     * Reads xml file and returns it as a string
     *
     * @return list of countries
     * @throws IOException if file not found
     */
    private String getCountriesAsString() throws IOException {
        InputStream xml = null;
        try {
            xml = getAssets().open("countries.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert xml != null;
        BufferedReader r = new BufferedReader(new InputStreamReader(xml));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }

        return total.toString();
    }

    private boolean checkConnection() { //Kolla om man har anslutning till internet
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (privacyBuilder != null && !checkIfAceptedPP() && !privacyBuilder.isShowing()) {
            displayPrivacyPolicyNotification();
        }
    }

}
