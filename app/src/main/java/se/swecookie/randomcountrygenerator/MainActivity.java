package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

    private List<String> countryList;

    private int delayInMillis = delayOrigin;
    private static final int delayOrigin = 20;

    private MediaPlayer mp;

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
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnRandom:
                btnRandom.setEnabled(false);
                btnOpen.setEnabled(false);
                startLoop();
                break;
            case R.id.btnOpen:
                if (checkConnection()) {
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
            imgCountry.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.mipmap.ic_launcher));
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

}
