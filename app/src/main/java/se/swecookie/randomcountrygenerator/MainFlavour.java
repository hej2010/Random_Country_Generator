package se.swecookie.randomcountrygenerator;

import android.view.View;

interface MainFlavour {
    void loadAds(MainActivity activity, Preferences preferences);
    void onButtonClicked(View view, MainActivity activity);
    void openLauncher(MainActivity activity);
}
