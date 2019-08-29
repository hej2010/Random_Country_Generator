package se.swecookie.randomcountrygenerator;

import android.view.View;

public interface MainFlavour {
    void loadAds(MainActivity activity, Preferences preferences);
    void onButtonClicked(View view, MainActivity activity);
    void openLauncher(MainActivity activity);
}
