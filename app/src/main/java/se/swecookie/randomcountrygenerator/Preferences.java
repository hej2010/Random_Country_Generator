package se.swecookie.randomcountrygenerator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

public class Preferences {
    private static final String PREF_NAME = "accepted3";
    private static final String PREF_ACCEPTED = "accepted";
    private static final String PREF_PERSONALISED_ADS = "pa";
    private static final String PREF_NAME_SETTINGS = "settings";
    private static final String PREF_ANIMATIONS = "animate";

    private final SharedPreferences prefsPrivacy, prefsSettings;

    Preferences(@NonNull Context context) {
        prefsPrivacy = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefsSettings = context.getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
    }

    private boolean getBoolean(@NonNull String pref) {
        boolean b = prefsPrivacy.getBoolean(pref, false);
        Log.e("return", "return " + b + " for " + pref);
        return b;
    }

    boolean isAcceptedPP() {
        return getBoolean(PREF_ACCEPTED);
    }

    public boolean noPersonalisedAds() {
        return !getBoolean(PREF_PERSONALISED_ADS);
    }

    void setAccepted(boolean accepted, boolean personalisedAds) {
        SharedPreferences.Editor e = prefsPrivacy.edit();
        e.putBoolean(PREF_ACCEPTED, accepted)
                .putBoolean(PREF_PERSONALISED_ADS, personalisedAds)
                .apply();
    }


    boolean isAnimationsEnabled() {
        return prefsSettings.getBoolean(PREF_ANIMATIONS, true);
    }

    void setAnimationsEnabled(boolean checked) {
        SharedPreferences.Editor editor = prefsSettings.edit();
        editor.putBoolean(PREF_ANIMATIONS, checked).apply();
    }
}
