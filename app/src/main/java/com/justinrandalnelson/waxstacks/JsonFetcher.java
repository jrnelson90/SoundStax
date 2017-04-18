package com.justinrandalnelson.waxstacks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static String getResultStringFromDiscogs(String url) throws IOException {

        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", HttpConst.USER_AGENT);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        if (Preferences.get(Preferences.USERNAME, "").length() == 0) {
            connection.addRequestProperty("Authorization", "Discogs token=" +
                    Preferences.get(Preferences.USERNAME, ""));
        } else {
            connection.addRequestProperty("Authorization", "Discogs key=" +
                    HttpConst.CONSUMER_KEY + ", secret=" + HttpConst.CONSUMER_SECRET);
        }

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

    JSONObject fetchReleaseRelease(String _releaseReleaseName) {
        JSONObject jsonBody = null;

        //Parse search into a URL friendly encoding.
        String release_name_encoded = "";
        try {
            release_name_encoded = URLEncoder.encode(_releaseReleaseName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // Get JSON object for passed release info.
        String searchString =
                "https://api.discogs.com/database/search?release_title=" + release_name_encoded;
        try {
            String jsonStr = getResultStringFromDiscogs(searchString);
            Log.i(TAG, "Full Received Release JSON: " + jsonStr);
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed Release JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse Release JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch Release items", ioe);
        }
        return jsonBody;
    }

    JSONObject fetchUserIdentity() {
        JSONObject jsonBody = null;

        // Get JSON object for passed release info.
        String userIdentityURL = "https://api.discogs.com/oauth/identity";
        try {

            HttpsURLConnection connection =
                    (HttpsURLConnection) new URL(userIdentityURL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "OAuth" +
                    "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                    ", oauth_nonce=" + ts +
                    ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                    ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                    Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                    ", oauth_signature_method=PLAINTEXT" +
                    ", oauth_timestamp=" + ts);
            connection.setRequestProperty("User-Agent", HttpConst.USER_AGENT);


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

            String jsonStr = sb.toString();
            Log.i(TAG, "Received User Identity JSON: " + jsonStr);
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed User Identity JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse User Identity JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch User Identity items", ioe);
        }
        return jsonBody;
    }

    JSONObject fetchUserCollection() {
        JSONObject jsonBody = null;

        // Get JSON object for passed release info.
        String userIdentityURL = "https://api.discogs.com/users/" +
                Preferences.get(Preferences.USERNAME, "") + "/collection/folders/0/releases";
        try {

            HttpsURLConnection connection =
                    (HttpsURLConnection) new URL(userIdentityURL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "OAuth" +
                    "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                    ", oauth_nonce=" + ts +
                    ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                    ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                    Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                    ", oauth_signature_method=PLAINTEXT" +
                    ", oauth_timestamp=" + ts);
            connection.setRequestProperty("User-Agent", HttpConst.USER_AGENT);


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

            String jsonStr = sb.toString();
            Log.i(TAG, "Received User Collection JSON");
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed User Collection JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse User Collection JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch User Collection items", ioe);
        }
        return jsonBody;
    }

    JSONObject fetchUserWantlist() {
        JSONObject jsonBody = null;

        // Get JSON object for passed release info.
        String userIdentityURL = "https://api.discogs.com/users/" +
                Preferences.get(Preferences.USERNAME, "") + "/wants";
        try {

            HttpsURLConnection connection =
                    (HttpsURLConnection) new URL(userIdentityURL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "OAuth" +
                    "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                    ", oauth_nonce=" + ts +
                    ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                    ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                    Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                    ", oauth_signature_method=PLAINTEXT" +
                    ", oauth_timestamp=" + ts);
            connection.setRequestProperty("User-Agent", HttpConst.USER_AGENT);


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

            String jsonStr = sb.toString();
            Log.i(TAG, "Received User Wantlist JSON");
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed User Wantlist JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse User Wantlist JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch User Wantlist items", ioe);
        }
        return jsonBody;
    }

    JSONObject fetchUserProfile() {
        JSONObject jsonBody = null;

        // Get JSON object for passed release info.
        String userIdentityURL = "https://api.discogs.com/users/" +
                Preferences.get(Preferences.USERNAME, "");
        try {

            HttpsURLConnection connection =
                    (HttpsURLConnection) new URL(userIdentityURL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "OAuth" +
                    "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                    ", oauth_nonce=" + ts +
                    ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                    ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                    Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                    ", oauth_signature_method=PLAINTEXT" +
                    ", oauth_timestamp=" + ts);
            connection.setRequestProperty("User-Agent", HttpConst.USER_AGENT);


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

            String jsonStr = sb.toString();
            Log.i(TAG, "Received User Profile JSON: " + jsonStr);
            jsonBody = new JSONObject(jsonStr);
            Log.i(TAG, "Successfully parsed User Profile JSON");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse User Profile JSON", e);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch User Profile items", ioe);
        }
        return jsonBody;
    }

    Bitmap fetchUserProfilePicture(String profilePicUrl) {
        Bitmap profileBitmap = null;

        // Get JSON object for passed release info.
        try {

            HttpsURLConnection connection =
                    (HttpsURLConnection) new URL(profilePicUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "OAuth" +
                    "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                    ", oauth_nonce=" + ts +
                    ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                    ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                    Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                    ", oauth_signature_method=PLAINTEXT" +
                    ", oauth_timestamp=" + ts);
            connection.setRequestProperty("User-Agent", HttpConst.USER_AGENT);


            if (connection.getResponseCode() == 200) {
                Log.i("Connection Type", "Profile Picture URL Success");
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

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            int bytesRead;
            byte[] buffer = new byte[51200];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            connection.disconnect();

            profileBitmap = BitmapFactory.
                    decodeByteArray(out.toByteArray(), 0, out.toByteArray().length);
            Log.i(TAG, "Received User Profile Picture: Bitmap created");
        } catch (IOException ioe) {
            Log.e(TAG, "Error Downloading User Profile Picture: ", ioe);
        }
        return profileBitmap;
    }
}
