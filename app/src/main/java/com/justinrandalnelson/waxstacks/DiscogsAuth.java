package com.justinrandalnelson.waxstacks;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jrnel on 4/12/2017.
 */

public class DiscogsAuth {
    private static String getAuthToken() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection)
                new URL(HttpConst.REQUEST_TOKEN_ENDPOINT_URL).openConnection();
        connection.addRequestProperty("User-Agent", HttpConst.USER_AGENT);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
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
}
