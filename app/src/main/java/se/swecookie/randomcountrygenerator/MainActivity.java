package se.swecookie.randomcountrygenerator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int exponentialValueOrigin = -2;
    private static final int delayOrigin = 20;
    private static final int maxDelay = 800;
    private static final double exponentialBase = 1.2;

    private ImageSwitcher imgCountry;
    private TextView txtCountryName;
    private Button btnRandom, btnOpen, btnSettings, btnOpenWiki;
    private CheckBox cBEnableAnimations;

    private int delayInMillis = delayOrigin;
    private int exponentialValue = exponentialValueOrigin;
    private int soundID;
    private boolean loadedSound = false;

    private List<Country> countryList;
    private List<Country> currentList;
    private Country selectedCountry = null;
    private final String[] continents = new String[]{"All", "Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America"};
    private AudioManager audioManager = null;
    private SoundPool soundPool;
    private Preferences preferences;
    private MainFlavour mainFlavour;
    private AppDatabase appDatabase;
    private Animation in, out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener((soundPool, i, i1) -> loadedSound = true);
        soundID = soundPool.load(this, R.raw.blip_short, 1);
        Object o = getSystemService(AUDIO_SERVICE);
        if (o instanceof AudioManager) {
            audioManager = (AudioManager) o;
        }
        preferences = new Preferences(this);
        appDatabase = AppDatabase.getAppDatabase(this);

        imgCountry = findViewById(R.id.imgCountryFlag);
        txtCountryName = findViewById(R.id.txtCountryName);
        btnRandom = findViewById(R.id.btnRandom);
        btnSettings = findViewById(R.id.btnSettings);
        btnOpen = findViewById(R.id.btnOpen);
        btnOpen.setVisibility(View.GONE);
        btnOpenWiki = findViewById(R.id.btnOpenWiki);
        btnOpenWiki.setVisibility(View.GONE);
        cBEnableAnimations = findViewById(R.id.cBEnableAnimations);

        in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        imgCountry.setFactory(() -> new ImageView(MainActivity.this));
        imgCountry.setImageResource(R.drawable.se);
        imgCountry.setInAnimation(in);
        imgCountry.setOutAnimation(out);

        cBEnableAnimations.setChecked(preferences.isAnimationsEnabled());

        countryList = getCountriesAsList();
        currentList = new ArrayList<>();
        onSelectContinent(continents[0]);

        mainFlavour = new MainActivityExtended();
        mainFlavour.loadAds(this, preferences);
    }

    public void onButtonClicked(View view) {
        int id = view.getId();
        if (id == R.id.btnRandom) {
            onNewCountryClicked();
        } else if (id == R.id.btnOpen) {
            Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + selectedCountry.getName().replace(" ", "+"));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } else if (id == R.id.txtAbout) {
            showAbout();
        } else if (id == R.id.btnSettings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick a continent");
            builder.setItems(continents, (dialog, which) -> onSelectContinent(continents[which]));
            builder.show();
        } else if (id == R.id.cBEnableAnimations) {
            preferences.setAnimationsEnabled(cBEnableAnimations.isChecked());
            if (!cBEnableAnimations.isChecked()) {
                delayInMillis = maxDelay;
            }
        } else if (id == R.id.txtCountryList) {
            showCountryList();
        } else if (id == R.id.btnOpenWiki) {
            Uri uri2 = Uri.parse("https://www.wikipedia.org/search-redirect.php?family=wikipedia&language=en&search=" + selectedCountry.getName() + "&language=en&go=Go");
            startActivity(new Intent(Intent.ACTION_VIEW, uri2));
            return;
        } else if (id == R.id.txtHistory) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            return;
        }
        mainFlavour.onButtonClicked(view, MainActivity.this);
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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.country_list, null);

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
            showRandomCountry();
            onLoopStopped();
        }
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("About");
        builder.setIcon(R.drawable.se);
        builder.setMessage(getString(R.string.main_about_message));
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        if (BuildConfig.FREE_VERSION) {
            builder.setNeutralButton("Privacy Policy", (dialogInterface, i) -> {
                preferences.setAccepted(false, false);
                finish();
                mainFlavour.openLauncher(MainActivity.this);
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startLoop() {
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                playSound();
                delayInMillis += Math.pow(exponentialBase, exponentialValue++);

                setDelay(delayInMillis / 2);
                showRandomCountry();
                //Log.e(TAG, "run: delay " + delayInMillis);

                if (delayInMillis >= maxDelay) {
                    onLoopStopped();
                    ha.removeCallbacks(this);
                } else {
                    ha.postDelayed(this, delayInMillis);
                }
            }

            private void setDelay(int delayInMillis) {
                in.setDuration(delayInMillis);
                out.setDuration(delayInMillis);
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
        if (audioManager == null) {
            return 0.5f;
        }
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
        new Thread(() -> appDatabase.countryDao().insertAll(new CountryHistory(selectedCountry.getName(), selectedCountry.getContinent(),
                selectedCountry.getCode(), System.currentTimeMillis()))).start();
    }

    private void showRandomCountry() {
        playSound();
        int random = (int) (Math.random() * (currentList.size()));

        Country lastCountry = selectedCountry;
        selectedCountry = currentList.get(random);

        // to not get the same two times in a row
        if (selectedCountry.equals(lastCountry)) {
            showRandomCountry();
            return;
        }

        setLayout(selectedCountry);
    }

    private void setLayout(Country country) {
        String countryCode = country.getCode();

        // if country code = "do", get file do1.png (reserved java keyword)
        if (countryCode.equals("DO")) {
            countryCode = "do1";
        }
        txtCountryName.setText(getString(R.string.main_country_name, country.getName(), countryCode, getContinentLong(selectedCountry.getContinent())));

        imgCountry.setImageResource(country.getDrawableID(this));
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

    static String getContinentLong(String selectedContinent) {
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
