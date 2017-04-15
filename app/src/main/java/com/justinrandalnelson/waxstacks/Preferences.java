package com.justinrandalnelson.waxstacks;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;

/**
 * Created by jrnel on 4/14/2017.
 */

public class Preferences extends PreferenceActivity {
    protected static SharedPreferences appPreferences;

    protected static String OAUTH_ACCESS_KEY = "oauthAccessKey";
    protected static String OAUTH_ACCESS_SECRET = "oauthAccessSecret";

    protected static String OauthRequestTokenSecret;
    protected static String OauthCallbackConfirmed;
    protected static String OauthUserVerifier;
    protected static String OauthAccessToken;
    protected static String OauthAccessTokenSecret;
}
