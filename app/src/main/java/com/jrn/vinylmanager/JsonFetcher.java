package com.jrn.vinylmanager;

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

    public static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.discogs.com/oauth/request_token";
    public static final String AUTHORIZATION_WEBSITE_URL = "http://www.discogs.com/oauth/authorize";
    public static final String ACCESS_TOKEN_ENDPOINT_URL = "http://api.discogs.com/oauth/access_token";
    public static final String CONSUMER_KEY = "XqiFKQsXTFMDJpxWsrue";
    public static final String CONSUMER_SECRET = "XjWXVtNXjieFeGMJoHDCvbnibIOBqkbu";
    public static final String CALLBACK_URL = "callback://discogs";
    public static final String USER_AGENT = "VinylManager/0.1 +com.jrn.vinylmanager";
    private static final String TAG = "JsonFetcher";
    private static final String URL = "https://api.discogs.com/releases/249504";

    private static String getStringFromUrl(String url) throws IOException {

        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.addRequestProperty("Authorization", "Discogs key=" + CONSUMER_KEY + ", secret=" + CONSUMER_SECRET);
//        connection.addRequestProperty("client_secret", CONSUMER_SECRET);
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
     * Fetch items json array.
     *
     * @return the json array
     */
    JSONObject fetchItems() {
        JSONObject jsonBody = null;
        try {
            String searchString = createArtistSearchURL("The Beatles");
            String jsonStr = getStringFromUrl(searchString);
            Log.i(TAG, "Full Received JSON: " + jsonStr);
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return jsonBody;
    }

    private String createArtistSearchURL(String artist_name) {
        String artist_name_encoded = "";
        try {
            artist_name_encoded = URLEncoder.encode(artist_name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
        return "https://api.discogs.com/database/search?artist=" + artist_name_encoded;
    }
}
