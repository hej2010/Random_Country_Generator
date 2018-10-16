package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imgCountry;
    private TextView txtCountryName;
    private Button btnRandom, btnOpen, btnSettings;
    private CheckBox cBEnableAnimations;

    private List<Country> countryList;
    private List<Country> currentList;

    private int delayInMillis = delayOrigin;
    private int exponentialValue = exponentialValueOrigin;

    private static final int exponentialValueOrigin = -2;
    private static final int delayOrigin = 20;
    private static final int maxDelay = 800;
    private static final double exponentialBase = 1.2;
    private static final String preferenceName = "settings";
    private static final String cBPreferenceName = "checked";
    private static final String PATH = "se.swecookie.randomcountrygenerator";

    private FirebaseAnalytics mFirebaseAnalytics;

    private MediaPlayer mp;

    private AlertDialog privacyBuilder;

    private final CharSequence continents[] = new CharSequence[]{"All", "Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp = MediaPlayer.create(this, R.raw.blip_short);

        imgCountry = findViewById(R.id.imgCountryFlag);
        txtCountryName = findViewById(R.id.txtCountryName);
        btnRandom = findViewById(R.id.btnRandom);
        btnSettings = findViewById(R.id.btnSettings);
        btnOpen = findViewById(R.id.btnOpen);
        btnOpen.setVisibility(View.GONE);
        cBEnableAnimations = findViewById(R.id.cBEnableAnimations);

        if (isAnimationsEnabled()) {
            cBEnableAnimations.setChecked(true);
        } else {
            cBEnableAnimations.setChecked(false);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, "ca-app-pub-2831297200743176~3098371641");

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        countryList = getCountriesAsList();
        currentList = new ArrayList<>();
        onSelectContinent(continents[0]);
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnRandom:
                if (notAcceptedPP()) {
                    displayPrivacyPolicyNotification();
                } else {
                    onNewCountryClicked();
                }
                break;
            case R.id.btnOpen:
                if (checkConnection()) {
                    sendToFirebase("Clicked Open");
                    Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + txtCountryName.getText().toString().replace(" ", "+"));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    showConnectionError();
                }
                break;
            case R.id.txtAbout:
                showAbout();
                break;
            case R.id.btnSettings:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Pick a continent");
                builder.setItems(continents, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSelectContinent(continents[which]);
                    }
                });
                builder.show();
                break;
            case R.id.cBEnableAnimations:
                saveNewCheckBoxState(cBEnableAnimations.isChecked());
                break;
            case R.id.txtCountryList:
                showCountryList();
                break;
        }
    }

    private void showCountryList() {
        StringBuilder sb = new StringBuilder();
        for (Country c : countryList) {
            sb.append(c.getName())
                    .append(", ")
                    .append(getContinentLong(c.getContinent()))
                    .append(", ")
                    .append(c.getCode())
                    .append("\n\n");
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.country_list, null);

        TextView textview = view.findViewById(R.id.txtList);
        textview.setText(sb.toString().trim());

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("All countries and islands");
        alertDialog.setView(view);
        alertDialog.setPositiveButton("Close", null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private boolean isAnimationsEnabled() {
        SharedPreferences prefs = getSharedPreferences(preferenceName, MODE_PRIVATE);
        return prefs.getBoolean(cBPreferenceName, true);
    }

    private void saveNewCheckBoxState(boolean checked) {
        SharedPreferences.Editor editor = getSharedPreferences(preferenceName, MODE_PRIVATE).edit();
        editor.putBoolean(cBPreferenceName, checked);
        editor.apply();
    }

    private void onSelectContinent(CharSequence continent) {
        String selectedShort = getContinentShort(continent.toString());
        btnSettings.setText(getString(R.string.main_settings, continent.toString()));
        currentList.clear();
        if (selectedShort.equals("All")) {
            currentList.addAll(countryList);
        } else {
            for (Country c : countryList) {
                if (c.getContinent().equals(selectedShort)) {
                    currentList.add(c);
                }
            }
        }
    }

    private void onNewCountryClicked() {
        if (isAnimationsEnabled()) {
            btnRandom.setEnabled(false);
            btnOpen.setEnabled(false);
            btnSettings.setEnabled(false);
            startLoop();
        } else {
            onLoopStopped();
            showRandomCountry();
        }
        sendToFirebase("Clicked Random");
    }

    private boolean notAcceptedPP() {
        SharedPreferences prefs = getSharedPreferences("accepted", MODE_PRIVATE);
        return !prefs.getBoolean("notAcceptedPP", false);
    }

    private void setAcceptedPP(boolean accepted) {
        SharedPreferences.Editor editor = getSharedPreferences("accepted", MODE_PRIVATE).edit();
        editor.putBoolean("notAcceptedPP", accepted);
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

    private void showConnectionError() { // If no connection
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
                showRandomCountry();

                delayInMillis += Math.pow(exponentialBase, exponentialValue++);

                if (delayInMillis >= maxDelay) {
                    onLoopStopped();
                    ha.removeCallbacks(this);
                } else {
                    ha.postDelayed(this, delayInMillis);
                }
            }
        }, delayInMillis);
    }

    private void onLoopStopped() {
        exponentialValue = exponentialValueOrigin;
        delayInMillis = delayOrigin;
        btnRandom.setEnabled(true);
        btnOpen.setVisibility(View.VISIBLE);
        btnOpen.setEnabled(true);
        btnSettings.setEnabled(true);
    }

    private void showRandomCountry() {
        mp.start();
        int random = (int) (Math.random() * (currentList.size()));

        final Country country = currentList.get(random);

        // to not get the same two times in a row
        if (country.getName().equals(txtCountryName.getText().toString())) {
            showRandomCountry();
            return;
        }

        setLayout(country.getName(), country.getCode());
    }

    private void setLayout(String countryName, String countryCode) {
        // if country code = "do", get file do1.png (reserved java keyword)
        if (countryCode.equals("DO")) {
            countryCode = "do1";
        }
        txtCountryName.setText(countryName);

        imgCountry.setImageBitmap(BitmapFactory.decodeResource(getResources(), getResources().
                getIdentifier(countryCode.toLowerCase(), "drawable", PATH)));
    }

    private boolean checkConnection() {
        boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                connected = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                connected = true;
            }
        }
        return connected;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (privacyBuilder != null && notAcceptedPP() && !privacyBuilder.isShowing()) {
            displayPrivacyPolicyNotification();
        }
    }

    private ArrayList<Country> getCountriesAsList() {
        ArrayList<Country> countries = new ArrayList<>();
        InputStream xml;
        try {
            xml = getAssets().open("countries.xml");
            BufferedReader r = new BufferedReader(new InputStreamReader(xml));

            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("code")) {
                    countries.add(Country.stringToCountry(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return countries;
    }

    private String getContinentShort(String selectedContinent) {
        // "All continents", "Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America"
        switch (selectedContinent) {
            case "All":
                return "All";
            case "Africa":
                return "AF";
            case "Antarctica":
                return "AN";
            case "Asia":
                return "AS";
            case "Europe":
                return "EU";
            case "North America":
                return "NA";
            case "Oceania":
                return "OC";
            case "South America":
                return "SA";
        }
        return "";
    }

    private String getContinentLong(String selectedContinent) {
        switch (selectedContinent) {
            case "All":
                return "All continents";
            case "AF":
                return "Africa";
            case "AN":
                return "Antarctica";
            case "AS":
                return "Asia";
            case "EU":
                return "Europe";
            case "NA":
                return "North America";
            case "OC":
                return "Oceania";
            case "SA":
                return "South America";
        }
        return "";
    }

}
