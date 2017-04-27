package com.justinrandalnelson.waxstacks;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;

/**
 * Basic persistent application preferences
 * Created by jrnel on 4/14/2017.
 */

public class Preferences extends PreferenceActivity {

    protected static SharedPreferences appPreferences;

    protected static String OAUTH_ACCESS_KEY = "oauthAccessKey";
    protected static String OAUTH_ACCESS_SECRET = "oauthAccessSecret";
    protected static String USERNAME = "username";
    protected static String USER_ID = "userId";
    protected static String USER_PROFILE = "userProfile";
    protected static String USER_PIC_DIR = "userPicDir";

    protected static void setPreferenceContext(SharedPreferences preferences) {
        appPreferences = preferences;
    }

    protected static String get(String _key, String _defaultValue) {
        try {
            return appPreferences.getString(_key, _defaultValue);
        } catch (Exception e) {
            return set(_key, _defaultValue);
        }
    }

    public static String set(String _key, String _value) {
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString(_key, _value);
        editor.apply();
        return _value;
    }
}
