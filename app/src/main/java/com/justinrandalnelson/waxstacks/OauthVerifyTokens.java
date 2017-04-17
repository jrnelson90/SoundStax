package com.justinrandalnelson.waxstacks;

/*
 * Simple Oauth Verification Tokens Object
 * Created by jrnel on 4/14/2017.
 */

class OauthVerifyTokens {
    private static String OauthRequestToken;
    private static String OauthRequestTokenSecret;
    private static String OauthUserVerifier;

    public OauthVerifyTokens() {
        OauthRequestToken = null;
        OauthRequestTokenSecret = null;
        OauthUserVerifier = null;
    }

    static String getOauthRequestToken() {
        return OauthRequestToken;
    }

    static void setOauthRequestToken(String oauthRequestToken) {
        OauthRequestToken = oauthRequestToken;
    }

    static String getOauthRequestTokenSecret() {
        return OauthRequestTokenSecret;
    }

    static void setOauthRequestTokenSecret(String oauthRequestTokenSecret) {
        OauthRequestTokenSecret = oauthRequestTokenSecret;
    }

    static String getOauthUserVerifier() {
        return OauthUserVerifier;
    }

    static void setOauthUserVerifier(String oauthUserVerifier) {
        OauthUserVerifier = oauthUserVerifier;
    }

}
