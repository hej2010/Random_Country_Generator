package se.swecookie.randomcountrygenerator;

import android.content.res.Resources;
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
    private Button btnRandom;

    private List<String> countryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countryList = new ArrayList<>();

        imgCountry = (ImageView) findViewById(R.id.imgCountryFlag);
        txtCountryName = (TextView) findViewById(R.id.txtCountryName);
        btnRandom = (Button) findViewById(R.id.btnRandom);

    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.btnRandom:
                randomCountry();
                break;
        }
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

}
