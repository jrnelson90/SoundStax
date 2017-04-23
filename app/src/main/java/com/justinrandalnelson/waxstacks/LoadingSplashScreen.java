package com.justinrandalnelson.waxstacks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Post Login Loading Screen
 * Created by jrnel on 4/23/2017.
 */

public class LoadingSplashScreen extends Activity {
    private UserCollectionDB mUserCollectionDB;
    private UserWantlistDB mUserWantlistDB;
    private JSONArray mUserCollectionJSON = new JSONArray();
    private JSONArray mUserWantlistJSON = new JSONArray();
    private JSONObject mUserProfileJSON = new JSONObject();
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Loading Splash", "Loading started");
        queue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.activity_splash_screen);
        if (Preferences.get(Preferences.USER_PROFILE, "").length() != 0) {
            mUserCollectionDB = UserCollectionDB.get(getApplicationContext());
            mUserWantlistDB = UserWantlistDB.get(getApplicationContext());
        }
        /*
         * Showing splashscreen while making network calls to download necessary
         * data before launching the app Will use AsyncTask to make http call
         */
        FetchUserIdentity();
    }

    private void FetchUserIdentity() {
        String userIdentityURL = "https://api.discogs.com/oauth/identity";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userIdentityURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String usernameString = response.getString("username");
                            Preferences.set(Preferences.USERNAME, usernameString);
                            Log.i("Set Username Pref:", Preferences.get(Preferences.USERNAME, ""));
                            String userIdString = response.getString("id");
                            Preferences.set(Preferences.USER_ID, userIdString);
                            Log.i("Set User ID Pref:", Preferences.get(Preferences.USER_ID, ""));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                            updateUsername();
                        mUserCollectionDB = UserCollectionDB.get(getApplicationContext());
                        mUserWantlistDB = UserWantlistDB.get(getApplicationContext());
                        Toast.makeText(getApplicationContext(),
                                "Logged in as " + Preferences.get(Preferences.USERNAME, "")
                                , Toast.LENGTH_SHORT)
                                .show();
                        FetchUserProfileJSON();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                        Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts);
                params.put("User-Agent", HttpConst.USER_AGENT);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);

    }

    private void FetchUserProfileJSON() {
        String userProfileURL = "https://api.discogs.com/users/" +
                Preferences.get(Preferences.USERNAME, "");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userProfileURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        mUserProfileJSON = response;
                        Log.i("Profile Request", "Received User Profile JSON");
                        Preferences.set(Preferences.USER_PROFILE, mUserProfileJSON.toString());
//                        updateProfilePicture();
                        String collectionFirstPageURL = "https://api.discogs.com/users/" +
                                Preferences.get(Preferences.USERNAME, "") +
                                "/collection/folders/0/releases?page=1&per_page=100&sort=added&sort_order=desc";
                        FetchUserCollectionJSON(collectionFirstPageURL);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                        Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts);
                params.put("User-Agent", HttpConst.USER_AGENT);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void FetchUserCollectionJSON(final String userCollectionURL) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userCollectionURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray currentPageArray = (JSONArray) response.get("releases");
                            if (mUserCollectionJSON.length() < 100) {
                                mUserCollectionJSON = currentPageArray;
                            } else {
                                mUserCollectionJSON = concatArray(mUserCollectionJSON, currentPageArray);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject paginationInfo = (JSONObject) response.get("pagination");
                            int currentListPage = (int) paginationInfo.get("page");
                            int totalListPages = (int) paginationInfo.get("pages");
                            if (currentListPage < totalListPages) {
                                String nextPageURL = userCollectionURL.replace(
                                        "page=" + String.valueOf(currentListPage) + "&",
                                        "page=" + String.valueOf(currentListPage + 1) + "&");
                                FetchUserCollectionJSON(nextPageURL);
                            } else {
                                Log.i("Collection Download", "All collection items have been downloaded");
                                String userWantlistURL = "https://api.discogs.com/users/" +
                                        Preferences.get(Preferences.USERNAME, "") +
                                        "/wants?page=1&per_page=100&sort=added&sort_order=desc";
                                FetchUserWantlistJSON(userWantlistURL);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                        Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts);
                params.put("User-Agent", HttpConst.USER_AGENT);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void FetchUserWantlistJSON(final String userWantlistURL) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userWantlistURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray currentPageArray = (JSONArray) response.get("wants");
                            if (mUserWantlistJSON.length() < 100) {
                                mUserWantlistJSON = currentPageArray;
                            } else {
                                mUserWantlistJSON = concatArray(mUserWantlistJSON, currentPageArray);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONObject paginationInfo = (JSONObject) response.get("pagination");
                            int currentListPage = (int) paginationInfo.get("page");
                            int totalListPages = (int) paginationInfo.get("pages");
                            if (currentListPage < totalListPages) {
                                String nextPageURL = userWantlistURL.replace(
                                        "page=" + String.valueOf(currentListPage) + "&",
                                        "page=" + String.valueOf(currentListPage + 1) + "&");
                                FetchUserWantlistJSON(nextPageURL);
                            } else {
                                Log.i("Wantlist Download", "All wantlist items have been downloaded");
                                extractCollectionData();
                                extractWantlistData();
                                Log.i("Loading Splash", "Loading complete");
                                Intent i = new Intent(LoadingSplashScreen.this, DashboardActivity.class);
                                startActivity(i);
                                // close this activity
                                finish();
//                                setPreviewThumbnails();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                        Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts);
                params.put("User-Agent", HttpConst.USER_AGENT);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void extractCollectionData() {
        try {
            if (mUserCollectionJSON.length() != mUserCollectionDB.getReleases().size()) {
                for (int i = 0; i < mUserCollectionJSON.length(); i++) {
                    JSONObject currentRelease = (JSONObject) mUserCollectionJSON.get(i);
                    JSONObject basicInfo = currentRelease.getJSONObject("basic_information");
                    String releaseTitle = basicInfo.getString("title");
                    String releaseYear = basicInfo.getString("year");
                    String releaseArtist = basicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
                    String releaseId = basicInfo.getString("id");
                    Release release = new Release();
                    release.setArtist(releaseArtist);
                    release.setYear(releaseYear);
                    release.setTitle(releaseTitle);
                    release.setReleaseId(releaseId);
                    release.setThumbUrl(basicInfo.getString("thumb"));
                    release.setThumbDir("");
                    mUserCollectionDB.addRelease(release);
                }
                Log.i("Collection Parse", "All releases in collection have been parsed to SQLite");
            } else {
                Log.i("Collection Parse", "No new releases to parse to SQLite");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractWantlistData() {
        try {
            if (mUserWantlistJSON.length() != mUserWantlistDB.getReleases().size()) {
                for (int i = 0; i < mUserWantlistJSON.length(); i++) {
                    JSONObject currentRelease = (JSONObject) mUserWantlistJSON.get(i);
                    JSONObject basicInfo = currentRelease.getJSONObject("basic_information");
                    String releaseTitle = basicInfo.getString("title");
                    String releaseYear = basicInfo.getString("year");
                    String releaseId = basicInfo.getString("id");
                    String releaseArtist = basicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
                    Release release = new Release();
                    release.setArtist(releaseArtist);
                    release.setYear(releaseYear);
                    release.setTitle(releaseTitle);
                    release.setReleaseId(releaseId);
                    release.setThumbUrl(basicInfo.getString("thumb"));
                    release.setThumbDir("");
                    mUserWantlistDB.addRelease(release);
                }
                Log.i("Wantlist Parse", "All releases in wantlist have been parsed to SQLite");
            } else {
                Log.i("Wantlist Parse", "No new releases to parse to SQLite");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONArray concatArray(JSONArray... arrs) throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

}
