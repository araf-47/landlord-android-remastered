package com.landlord.android.core.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "landlord_session_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LANDLORD_ID = "landlord_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";

    private static volatile SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void loginDemoLandlord() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_LANDLORD_ID, 101L)
                .putString(KEY_USERNAME, "araf.landlord")
                .putString(KEY_DISPLAY_NAME, "Arafat Hossain")
                .putString(KEY_EMAIL, "landlord.arafat@gmail.com")
                .putString(KEY_PHONE, "+880 1711-987654")
                .apply();
    }

    public void loginCustom(String username, String password) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_LANDLORD_ID, 102L)
                .putString(KEY_USERNAME, username)
                .putString(KEY_DISPLAY_NAME, username)
                .putString(KEY_EMAIL, username.toLowerCase() + "@landlord.io")
                .putString(KEY_PHONE, "+880 1700-000000")
                .apply();
    }

    public long getLandlordId() {
        return prefs.getLong(KEY_LANDLORD_ID, 101L);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "araf.landlord");
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "Arafat Hossain");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "landlord.arafat@gmail.com");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "+880 1711-987654");
    }

    public void updateProfile(String displayName, String email, String phone) {
        prefs.edit()
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PHONE, phone)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
