package se.swecookie.randomcountrygenerator;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivityExtended implements MainFlavour {

    @Override
    public void loadAds(MainActivity activity, Preferences preferences) {
        MobileAds.initialize(activity);

        AdView mAdView = activity.findViewById(R.id.adView);
        AdRequest.Builder adRequest = new AdRequest.Builder();

        Bundle extras = new Bundle();
        if (preferences.noPersonalisedAds()) {
            extras.putString("npa", "1");
        }

        mAdView.loadAd(adRequest.addNetworkExtrasBundle(AdMobAdapter.class, extras).build());
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
