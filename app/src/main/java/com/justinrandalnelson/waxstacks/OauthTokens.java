package com.justinrandalnelson.waxstacks;

/*
 * Simple Oauth Toket Object
 * Created by jrnel on 4/14/2017.
 */

public class OauthTokens {
    private static String OauthRequestToken;
    private static String OauthRequestTokenSecret;
    private static String OauthCallbackConfirmed;
    private static String OauthUserVerifier;
    private static String OauthAccessToken;
    private static String OauthAccessTokenSecret;

    public OauthTokens() {
        OauthRequestToken = null;
        OauthRequestTokenSecret = null;
        OauthCallbackConfirmed = null;
        OauthUserVerifier = null;
        OauthAccessToken = null;
        OauthAccessTokenSecret = null;
    }

    public static String getOauthRequestToken() {
        return OauthRequestToken;
    }

    public static void setOauthRequestToken(String oauthRequestToken) {
        OauthRequestToken = oauthRequestToken;
    }

    public static String getOauthRequestTokenSecret() {
        return OauthRequestTokenSecret;
    }

    public static void setOauthRequestTokenSecret(String oauthRequestTokenSecret) {
        OauthRequestTokenSecret = oauthRequestTokenSecret;
    }

    public static String getOauthCallbackConfirmed() {
        return OauthCallbackConfirmed;
    }

    public static void setOauthCallbackConfirmed(String oauthCallbackConfirmed) {
        OauthCallbackConfirmed = oauthCallbackConfirmed;
    }

    public static String getOauthUserVerifier() {
        return OauthUserVerifier;
    }

    public static void setOauthUserVerifier(String oauthUserVerifier) {
        OauthUserVerifier = oauthUserVerifier;
    }

    public static String getOauthAccessToken() {
        return OauthAccessToken;
    }

    public static void setOauthAccessToken(String oauthAccessToken) {
        OauthAccessToken = oauthAccessToken;
    }

    public static String getOauthAccessTokenSecret() {
        return OauthAccessTokenSecret;
    }

    public static void setOauthAccessTokenSecret(String oauthAccessTokenSecret) {
        OauthAccessTokenSecret = oauthAccessTokenSecret;
    }
}
