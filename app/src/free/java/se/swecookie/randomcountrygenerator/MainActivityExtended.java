package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.applovin.mediation.ads.MaxAdView;
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
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                createBannerAd(activity);
            }
        });

        MobileAds.initialize(activity, initializationStatus -> {
        });
    }

    private void createBannerAd(MainActivity activity) {
        MaxAdView adView = new MaxAdView("a47c06d248dbab03", activity);

        // Stretch to the width of the screen for banners to be fully functional
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = activity.getResources().getDimensionPixelSize(R.dimen.banner_height);

        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));

        // Set background or background color for banners to be fully functional
        adView.setBackgroundColor(activity.getResources().getColor(R.color.colorPrivacyBG));

        ViewGroup rootView = activity.findViewById(R.id.adView);
        rootView.addView(adView);

        // Load the ad
        adView.loadAd();
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
