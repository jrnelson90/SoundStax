package com.soundstax.soundstax;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Post-Login Loading Screen
 * Created by jrnel on 4/23/2017.
 */

public class LoadingSplashActivity extends Activity {
    private UserCollectionDB mUserCollectionDB;
    private UserWantlistDB mUserWantlistDB;
    private JSONArray mUserCollectionJSON = new JSONArray();
    private JSONArray mUserWantlistJSON = new JSONArray();
    private JSONObject mUserProfileJSON = new JSONObject();
    private JSONObject mUserFoldersJSON = new JSONObject();
    private RequestQueue queue;
    private ProgressBar loadingBar;
    private TextView loadingText;
    private int progressTotal = 0;
    private int progressMade = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Loading Splash", "Loading started");
        queue = VolleyRequestQueue.getInstance(getApplicationContext()).getRequestQueue();
        setContentView(R.layout.activity_splash_screen);
        loadingBar = (ProgressBar) findViewById(R.id.loading_account_progress_bar);
        loadingText = (TextView) findViewById(R.id.loading_account_text_view);
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
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
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
                        int wantlistNum = 0;
                        int collectionNum = 0;
                        try {
                            wantlistNum = mUserProfileJSON.getInt("num_wantlist");
                            collectionNum = mUserProfileJSON.getInt("num_collection");
                            progressTotal = 6 + (collectionNum / 100) + (wantlistNum / 100);
                            Log.i("Profile Request", "Received User Profile JSON");
                            Preferences.set(Preferences.USER_PROFILE, mUserProfileJSON.toString());


                            String loadedProfileMessage =
                                    "Loaded Profile for " + Preferences.get(Preferences.USERNAME, "");
                            loadingText.setText(loadedProfileMessage);
                            loadingBar.setMax(progressTotal);
                            loadingBar.setProgress(++progressMade);
                            String collectionFirstPageURL = "https://api.discogs.com/users/" +
                                    Preferences.get(Preferences.USERNAME, "") +
                                    "/collection/folders/0/releases?page=1&per_page=100&sort=added&sort_order=desc";
                            FetchUserCollectionJSON(collectionFirstPageURL);

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
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
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

                            JSONObject paginationInfo = (JSONObject) response.get("pagination");
                            int currentListPage = (int) paginationInfo.get("page");
                            int totalListPages = (int) paginationInfo.get("pages");
                            String loadedCollectionMessage =
                                    "Loaded Collection Page " + String.valueOf(currentListPage)
                                            + " of " + String.valueOf(totalListPages);
                            loadingText.setText(loadedCollectionMessage);
                            loadingBar.setProgress(++progressMade);
                            if (currentListPage < totalListPages) {
                                String nextPageURL = userCollectionURL.replace(
                                        "page=" + String.valueOf(currentListPage) + "&",
                                        "page=" + String.valueOf(currentListPage + 1) + "&");
                                FetchUserCollectionJSON(nextPageURL);
                            } else {
                                Log.i("Collection Download", "All collection items have been downloaded");
                                String userWantlistURL = "https://api.discogs.com/users/" +
                                        Preferences.get(Preferences.USERNAME, "") +
                                        "/wants?page=1&per_page=100&sort=added&sort_order=asc";
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
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
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
                            String loadedWantlistMessage =
                                    "Loaded Wantlist Page " + String.valueOf(currentListPage)
                                            + " of " + String.valueOf(totalListPages);
                            loadingText.setText(loadedWantlistMessage);
                            loadingBar.setProgress(++progressMade);
                            if (currentListPage < totalListPages) {
                                String nextPageURL = userWantlistURL.replace(
                                        "page=" + String.valueOf(currentListPage) + "&",
                                        "page=" + String.valueOf(currentListPage + 1) + "&");
                                FetchUserWantlistJSON(nextPageURL);
                            } else {
                                FetchUserFoldersJSON();
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
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
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

    private void FetchUserFoldersJSON() {
        // GET /users/{username}/collection/folders
        String folderRequestURL = "https://api.discogs.com/users/" +
                Preferences.get(Preferences.USERNAME, "") + "/collection/folders";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, folderRequestURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mUserFoldersJSON = response;
                        String loadedFoldersMessage = "Loaded User Folders";
                        loadingText.setText(loadedFoldersMessage);
                        loadingBar.setProgress(++progressMade);

                        Log.i("Folders Download", "All folder have been downloaded");
                        extractFoldersData();
                        extractCollectionData();
                        extractWantlistData();
                        Log.i("Loading Splash", "Loading complete");
                        Intent i = new Intent(LoadingSplashActivity.this, DashboardActivity.class);
                        startActivity(i);
                        // close this activity
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                }) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;
                if (mStatusCode == 204) {
//                    UserCollectionDB.get(getActivity()).deleteRelease(mRelease);
//                    getActivity().finish();
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
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

    private void extractFoldersData() {
        JSONArray foldersArray = null;
        try {
            foldersArray = mUserFoldersJSON.getJSONArray("folders");
            for (int i = 0; i < foldersArray.length(); i++) {
                UserFolders.Folder currentFolder = new UserFolders.Folder();
                JSONObject currentFolderJSON = foldersArray.getJSONObject(i);
                currentFolder.setId(currentFolderJSON.getString("id"));
                currentFolder.setName(currentFolderJSON.getString("name"));
                currentFolder.setCount(currentFolderJSON.getString("count"));
                UserFolders.sFolderArrayList.add(currentFolder);
            }
            Log.i("Folder Extract", "All Folder info extracted");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void extractCollectionData() {
        try {
            if (mUserCollectionJSON.length() != mUserCollectionDB.getReleases().size()) {
                loadingBar.setProgress(++progressMade);
                for (int i = 0; i < mUserCollectionJSON.length(); i++) {
                    JSONObject currentRelease = (JSONObject) mUserCollectionJSON.get(i);
                    JSONObject basicInfo = currentRelease.getJSONObject("basic_information");
                    String releaseTitle = basicInfo.getString("title");
                    String releaseYear = basicInfo.getString("year");
                    String releaseArtist = basicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
                    String releaseId = basicInfo.getString("id");
                    String instanceId = currentRelease.getString("instance_id");
                    String folderId = currentRelease.getString("folder_id");

                    String folderNameString = "";
                    JSONArray foldersArray = mUserFoldersJSON.getJSONArray("folders");
                    for (int j = 0; j < foldersArray.length(); j++) {
                        JSONObject currentFolderJSON = foldersArray.getJSONObject(j);
                        if (folderId.equals(currentFolderJSON.getString("id"))) {
                            folderNameString = currentFolderJSON.getString("name");
                        }
                    }

                    String dateAdded = currentRelease.getString("date_added");
                    JSONObject formatInfo = basicInfo.getJSONArray("formats").getJSONObject(0);
                    String formatName = formatInfo.getString("name");
                    String formatQty = formatInfo.getString("qty");
                    String formatDescriptions = "";
                    if (formatInfo.has("descriptions")) {
                        JSONArray formatDescriptionsArray = formatInfo.getJSONArray("descriptions");
                        formatDescriptions = formatDescriptionsArray.toString();
                    }

                    String formatText = "";
                    if (formatInfo.has("text")) {
                        formatText = formatInfo.getString("text");
                    }

                    Release release = new Release();
                    release.setArtist(releaseArtist);
                    release.setYear(releaseYear);
                    release.setTitle(releaseTitle);
                    release.setReleaseId(releaseId);
                    release.setInstanceId(instanceId);
                    release.setFolderId(folderId);
                    release.setFolderName(folderNameString);
                    release.setFormatName(formatName);
                    release.setFormatQty(formatQty);
                    release.setFormatDescriptions(formatDescriptions);
                    release.setFormatText(formatText);
                    release.setDateAdded(dateAdded);
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
                loadingBar.setProgress(++progressMade);
                for (int i = 0; i < mUserWantlistJSON.length(); i++) {
                    JSONObject currentRelease = (JSONObject) mUserWantlistJSON.get(i);
                    JSONObject basicInfo = currentRelease.getJSONObject("basic_information");
                    String releaseTitle = basicInfo.getString("title");
                    String releaseYear = basicInfo.getString("year");
                    String releaseId = basicInfo.getString("id");
                    String releaseArtist = basicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
                    JSONObject formatInfo = basicInfo.getJSONArray("formats").getJSONObject(0);
                    String formatName = formatInfo.getString("name");
                    String formatQty = formatInfo.getString("qty");
                    String formatDescriptions = "";
                    if (formatInfo.has("descriptions")) {
                        JSONArray formatDescriptionsArray = formatInfo.getJSONArray("descriptions");
                        formatDescriptions = formatDescriptionsArray.toString();
                    }
                    String formatText = "";
                    if (formatInfo.has("text")) {
                        formatText = formatInfo.getString("text");
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
                    Date now = new Date();
                    String dateAdded = sdf.format(now);

                    Release release = new Release();
                    release.setArtist(releaseArtist);
                    release.setYear(releaseYear);
                    release.setTitle(releaseTitle);
                    release.setReleaseId(releaseId);
                    release.setFormatName(formatName);
                    release.setFormatQty(formatQty);
                    release.setFormatDescriptions(formatDescriptions);
                    release.setFormatText(formatText);
                    release.setDateAdded(dateAdded);
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
