package com.jrn.waxstack;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jrnel on 4/14/2017.
 */

class OauthTokenFetcher {
    private static final String TAG = "OauthTokenFetcher";

    private static String getRequestToken() throws IOException {
        // GET https://api.discogs.com/oauth/request_token
        HttpsURLConnection connection = (HttpsURLConnection)
                new URL(HttpConst.REQUEST_TOKEN_ENDPOINT_URL).openConnection();

        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();

        connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.addRequestProperty("Authorization", "OAuth" +
                "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                ", oauth_nonce=" + ts +
                ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                ", oauth_signature_method=PLAINTEXT" +
                ", oauth_timestamp=" + ts +
                ", oauth_callback=" + HttpConst.CALLBACK_URL);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.addRequestProperty("User-Agent", HttpConst.USER_AGENT);

        if (connection.getResponseCode() == 200) {
            Log.i("Connection Type", "Success");
            Log.i("Connection Code", String.valueOf(connection.getResponseCode()));
            Log.i("Connection Message", connection.getResponseMessage());
            // Success
            // Further processing here
        } else {
            // Error handling code goes here
            Log.i("Connection Type", "Failed");
            Log.i("Connection Code", String.valueOf(connection.getResponseCode()));
            Log.i("Connection Message", connection.getResponseMessage());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        String NEWLINE = System.lineSeparator();
        while ((line = in.readLine()) != null) {
            sb.append(line).append(NEWLINE);
        }
        connection.disconnect();

        return sb.toString();
    }

    String[] fetchRequestToken() {
        // Get JSON array for oauth token.
        String[] oauthReturn = new String[3];
        try {
            String jsonStr = getRequestToken();
            oauthReturn = jsonStr.split("&");
            Log.i(TAG, "Received Request Oauth Token");
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch Request Token", ioe);
        }
        return oauthReturn;
    }

    String[] fetchOauthAccessToken(String[] _passedOauthVerify) {
        // Get JSON array for oauth token.
        String[] oauthAccessReturn = new String[3];
        try {
            String jsonStr = getOauthAccessToken(_passedOauthVerify);
            oauthAccessReturn = jsonStr.split("&");
            Log.i(TAG, "Received Access Oauth Token");
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch Request Token", ioe);
        }
        return oauthAccessReturn;
    }

    private String getOauthAccessToken(String[] _passedOauthVerify) throws IOException {
        // POST https://api.discogs.com/oauth/access_token

        HttpsURLConnection connection = (HttpsURLConnection)
                new URL(HttpConst.ACCESS_TOKEN_ENDPOINT_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        connection.addRequestProperty("Authorization", "OAuth" +
                "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                ", oauth_nonce=" + ts +
                ", oauth_token=" + _passedOauthVerify[0] +
                ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                OauthTokens.getOauthRequestTokenSecret() +
                ", oauth_signature_method=PLAINTEXT" +
                ", oauth_timestamp=" + ts +
                ", oauth_verifier=" + _passedOauthVerify[1]);
        connection.setRequestProperty("User-Agent", HttpConst.USER_AGENT);
        connection.connect();

        // Read POST response code
        if (connection.getResponseCode() == 200) {
            Log.i("Connection Result", "Success");
            Log.i("Connection Code", String.valueOf(connection.getResponseCode()));
            Log.i("Connection Message", connection.getResponseMessage());
        } else {
            // Error handling code goes here
            Log.i("Connection Result", "Failed");
            Log.i("Connection Code", String.valueOf(connection.getResponseCode()));
            Log.i("Connection Message", connection.getResponseMessage());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        String NEWLINE = System.lineSeparator();
        while ((line = in.readLine()) != null) {
            sb.append(line).append(NEWLINE);
        }
        connection.disconnect();

        return sb.toString();
    }
}
