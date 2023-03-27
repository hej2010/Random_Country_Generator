package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

public class MainActivityExtended implements MainFlavour {

    @Override
    public void loadAds(MainActivity activity, Preferences preferences) {

        RequestConfiguration conf = new RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T) // G, PG, T, MA (3, 7, 12, 16/18)
                .build();
        MobileAds.setRequestConfiguration(conf);

        AppLovinPrivacySettings.setHasUserConsent(preferences.noPersonalisedAds(), activity);

        AppLovinSdk.getInstance(activity).setMediationProvider(AppLovinMediationProvider.MAX);
        AppLovinSdk.initializeSdk(activity, configuration -> {
        });

        MobileAds.initialize(activity, initializationStatus -> {
        });
    }

    @Override
    public void onButtonClicked(View view, MainActivity activity) {
        if (view.getId() == R.id.txtAdFree) {
            showAdFree(activity);
        }
    }

    @Override
    public void openLauncher(MainActivity activity) {
        activity.startActivity(new Intent(activity, LauncherActivity.class));
    }

    private void showAdFree(MainActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.main_remove_ads_title));
        builder.setMessage(activity.getString(R.string.main_remove_ads_message));
        builder.setPositiveButton(activity.getString(R.string.main_remove_ads_open), (dialog, which) -> {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=se.swecookie.randomcountrygenerator.pro");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
