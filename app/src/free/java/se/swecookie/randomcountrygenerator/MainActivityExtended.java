package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinCmpService;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.applovin.sdk.AppLovinSdkUtils;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

public class MainActivityExtended implements MainFlavour {

    @Override
    public void loadAds(MainActivity activity, Preferences preferences) {
        RequestConfiguration conf = new RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T) // G, PG, T, MA (3, 7, 12, 16/18)
                .build();
        MobileAds.setRequestConfiguration(conf);

        AppLovinPrivacySettings.setIsAgeRestrictedUser(false, activity);

        AppLovinSdkSettings settings = new AppLovinSdkSettings(activity);
        settings.getTermsAndPrivacyPolicyFlowSettings().setEnabled(true);
        settings.getTermsAndPrivacyPolicyFlowSettings().setPrivacyPolicyUri(Uri.parse("https://arctosoft.com/apps/random-country-selector/privacy-policy/"));

        // Terms of Service URL is optional
        //settings.getTermsAndPrivacyPolicyFlowSettings().setTermsOfServiceUri( Uri.parse( "https://your_company_name.com/terms_of_service" ) );

        AppLovinSdk.getInstance(settings, activity).setMediationProvider(AppLovinMediationProvider.MAX);
        AppLovinSdk.initializeSdk(activity, configuration -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                createBannerAd(activity);
            }
        });
    }

    private void createBannerAd(MainActivity activity) {
        MaxAdView adView = new MaxAdView("a47c06d248dbab03", activity);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Get the adaptive banner height.
        int heightDp = MaxAdFormat.BANNER.getAdaptiveSize(activity).getHeight();
        int heightPx = AppLovinSdkUtils.dpToPx(activity, heightDp);

        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
        adView.setExtraParameter("adaptive_banner", "true");

        // Set background or background color for banners to be fully functional
        //adView.setBackgroundColor(R.color.);

        //ViewGroup rootView = findViewById(android.R.id.content);
        ViewGroup rootView = activity.findViewById(R.id.adView);
        rootView.addView(adView);

        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(@NonNull MaxAd maxAd) {
                adView.setLayoutParams(new FrameLayout.LayoutParams(width, AppLovinSdkUtils.dpToPx(activity, maxAd.getSize().getHeight())));
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        // Load the ad
        adView.loadAd();
        adView.startAutoRefresh();
    }

    @Override
    public void onButtonClicked(View view, MainActivity activity) {
        if (view.getId() == R.id.txtAdFree) {
            showAdFree(activity);
        }
    }

    @Override
    public void openLauncher(MainActivity activity) {
        AppLovinCmpService cmpService = AppLovinSdk.getInstance(activity).getCmpService();
        cmpService.showCmpForExistingUser(activity, error -> {
            if (null == error) {
                // The CMP alert was shown successfully.
            }
        });
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
