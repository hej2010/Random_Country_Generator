package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imgCountry;
    private TextView txtCountryName;
    private Button btnRandom, btnOpen, btnSettings, btnOpenWiki;
    private CheckBox cBEnableAnimations;

    private List<Country> countryList;
    private List<Country> currentList;

    private CustomTabsIntent customTabsIntent;

    private int delayInMillis = delayOrigin;
    private int exponentialValue = exponentialValueOrigin;
    private AudioManager audioManager;

    private static final int exponentialValueOrigin = -2;
    private static final int delayOrigin = 20;
    private static final int maxDelay = 800;
    private static final double exponentialBase = 1.2;

    private SoundPool soundPool;
    private int soundID;
    private boolean loadedSound = false;
    private Preferences preferences;

    private final CharSequence[] continents = new CharSequence[]{"All", "Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                loadedSound = true;
            }
        });
        soundID = soundPool.load(this, R.raw.blip_short, 1);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        preferences = new Preferences(this);

        imgCountry = findViewById(R.id.imgCountryFlag);
        txtCountryName = findViewById(R.id.txtCountryName);
        btnRandom = findViewById(R.id.btnRandom);
        btnSettings = findViewById(R.id.btnSettings);
        btnOpen = findViewById(R.id.btnOpen);
        btnOpen.setVisibility(View.GONE);
        btnOpenWiki = findViewById(R.id.btnOpenWiki);
        btnOpenWiki.setVisibility(View.GONE);
        cBEnableAnimations = findViewById(R.id.cBEnableAnimations);

        if (preferences.isAnimationsEnabled()) {
            cBEnableAnimations.setChecked(true);
        } else {
            cBEnableAnimations.setChecked(false);
        }

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorDark));
        builder.enableUrlBarHiding();
        builder.setShowTitle(true);
        customTabsIntent = builder.build();

        countryList = getCountriesAsList();
        currentList = new ArrayList<>();
        onSelectContinent(continents[0]);

        loadAds();
    }

    private void loadAds() {
        MobileAds.initialize(this, "ca-app-pub-2831297200743176~3098371641");

        AdView mAdView = findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();

        Bundle extras = new Bundle();
        if (preferences.noPersonalisedAds()) {
            extras.putString("npa", "1");
        }

        mAdView.loadAd(adRequest.addNetworkExtrasBundle(AdMobAdapter.class, extras).build());
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnRandom:
                onNewCountryClicked();
                break;
            case R.id.btnOpen:
                if (checkConnection()) {
                    Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + txtCountryName.getText().toString().replace(" ", "+"));
                    customTabsIntent.launchUrl(this, uri);
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
                preferences.setAnimationsEnabled(cBEnableAnimations.isChecked());
                break;
            case R.id.txtCountryList:
                showCountryList();
                break;
            case R.id.btnOpenWiki:
                if (checkConnection()) {
                    Uri uri = Uri.parse("https://www.wikipedia.org/search-redirect.php?family=wikipedia&language=en&search=" + txtCountryName.getText().toString() + "&language=en&go=Go");
                    customTabsIntent.launchUrl(this, uri);
                } else {
                    showConnectionError();
                }
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
        if (preferences.isAnimationsEnabled()) {
            btnRandom.setEnabled(false);
            btnOpen.setEnabled(false);
            btnOpenWiki.setEnabled(false);
            btnSettings.setEnabled(false);
            startLoop();
        } else {
            onLoopStopped();
            showRandomCountry();
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
        builder.setNeutralButton("Privacy Policy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                preferences.setAccepted(false, false);
                finish();
                startActivity(new Intent(MainActivity.this, LauncherActivity.class));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showConnectionError() {
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
                playSound();
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

    private void playSound() {
        if (loadedSound) {
            float volume = getVolume();
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
        }
    }

    private float getVolume() {
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return actualVolume / maxVolume;
    }

    private void onLoopStopped() {
        exponentialValue = exponentialValueOrigin;
        delayInMillis = delayOrigin;
        btnRandom.setEnabled(true);
        btnOpen.setVisibility(View.VISIBLE);
        btnOpen.setEnabled(true);
        btnOpenWiki.setVisibility(View.VISIBLE);
        btnOpenWiki.setEnabled(true);
        btnSettings.setEnabled(true);
    }

    private void showRandomCountry() {
        playSound();
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
                getIdentifier(countryCode.toLowerCase(), "drawable", BuildConfig.APPLICATION_ID)));
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
