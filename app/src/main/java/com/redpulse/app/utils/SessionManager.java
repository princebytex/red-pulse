package com.redpulse.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "red_pulse_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "full_name";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(String uid, String fullName, String email) {
        preferences.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_NAME, fullName)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public String getUid() {
        return preferences.getString(KEY_UID, null);
    }
}
