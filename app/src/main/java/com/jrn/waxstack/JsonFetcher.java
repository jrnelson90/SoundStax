package com.jrn.waxstack;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Fetches JSON Information from Discogs
 * Created by jrnel on 3/19/2017.
 */
class JsonFetcher {
    private static final String TAG = "JsonFetcher";
//    private static final String RICK_URL = "https://api.discogs.com/releases/249504";

    private static String getRequestToken() throws IOException {
//        GET https://api.discogs.com/oauth/request_token
        HttpsURLConnection connection = (HttpsURLConnection)
                new URL(HttpConst.REQUEST_TOKEN_ENDPOINT_URL).openConnection();

//        Content-Type: application/x-www-form-urlencoded
//        Authorization:
//        OAuth oauth_consumer_key="your_consumer_key",
//                oauth_nonce="random_string_or_timestamp",
//                oauth_signature="your_consumer_secret&",
//                oauth_signature_method="PLAINTEXT",
//                oauth_timestamp="current_timestamp",
//                oauth_callback="your_callback"
//        User-Agent: some_user_agent
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

    private static String getResultStringFromDiscogs(String url) throws IOException {

        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", HttpConst.USER_AGENT);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.addRequestProperty("Authorization", "Discogs key=" + HttpConst.CONSUMER_KEY +
                ", secret=" + HttpConst.CONSUMER_SECRET);
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

    /**
     * Fetch Discogs Artist items, return as a JSONObject.
     *
     * @return the json array
     */
    JSONObject fetchArtist(String _artistName) {
        JSONObject jsonBody = null;

        //Parse search into a URL friendly encoding.
        String artist_name_encoded = "";
        try {
            artist_name_encoded = URLEncoder.encode(_artistName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // Get JSON object for passed artist info.
        String searchString =
                "https://api.discogs.com/database/search?artist=" + artist_name_encoded;
        try {
            String jsonStr = getResultStringFromDiscogs(searchString);
            Log.i(TAG, "Full Received Artist JSON: " + jsonStr);
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed Artist JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse Artist JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch Artist items", ioe);
        }
        return jsonBody;
    }

    JSONObject fetchAlbumRelease(String _albumReleaseName) {
        JSONObject jsonBody = null;

        //Parse search into a URL friendly encoding.
        String album_name_encoded = "";
        try {
            album_name_encoded = URLEncoder.encode(_albumReleaseName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // Get JSON object for passed album info.
        String searchString =
                "https://api.discogs.com/database/search?release_title=" + album_name_encoded;
        try {
            String jsonStr = getResultStringFromDiscogs(searchString);
            Log.i(TAG, "Full Received Album JSON: " + jsonStr);
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed Album JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse Album JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch Album items", ioe);
        }
        return jsonBody;
    }

    String[] fetchRequestToken() {
        // Get JSON array for oauth token.
        String[] oauthReturn = new String[3];
        try {
            String jsonStr = getRequestToken();
            Log.i(TAG, "Received oauth Token string: " + jsonStr);
            oauthReturn = jsonStr.split("&");
            Log.i(TAG, "Received oauth Token Secret: " + oauthReturn[0]);
            Log.i(TAG, "Received oauth Token: " + oauthReturn[1]);
            Log.i(TAG, "Oauth Callback Confirmed: " + oauthReturn[2]);

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
            Log.i(TAG, "Received oauth Token string: " + jsonStr);
            oauthAccessReturn = jsonStr.split("&");
            Log.i(TAG, "Received oauth Token Secret: " + oauthAccessReturn[0]);
            Log.i(TAG, "Received oauth Token: " + oauthAccessReturn[1]);
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
            // Success
            // Further processing here
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
