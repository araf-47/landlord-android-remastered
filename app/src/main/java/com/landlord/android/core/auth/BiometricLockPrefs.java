package com.landlord.android.core.auth;

import android.content.Context;
import android.content.SharedPreferences;

public final class BiometricLockPrefs {

    private static final String PREF_NAME = "biometric_lock_prefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    private BiometricLockPrefs() {}

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }
}
