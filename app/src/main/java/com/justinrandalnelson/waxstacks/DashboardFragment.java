package com.justinrandalnelson.waxstacks;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard fragment
 * Created by jrnel on 4/14/2017.
 */

public class DashboardFragment extends Fragment {
    private UserCollectionDB mUserCollectionDB;
    private UserWantlistDB mUserWantlistDB;
    private JSONObject mUserInfoJSON = new JSONObject();
    private JSONArray mUserCollectionJSON = new JSONArray();
    private JSONArray mUserWantlistJSON = new JSONArray();
    private JSONObject mUserProfileJSON = new JSONObject();
    private LinearLayout mCollectionLinearLayout;
    private LinearLayout mWantlistLinearLayout;
    private TextView mUsernameLabel;
    private ImageView mUserProfilePicture;
    private ArrayList<Bitmap> mReleaseBitmaps = new ArrayList<>();
    private RequestQueue queue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Preferences.setPreferenceContext(PreferenceManager.getDefaultSharedPreferences(getContext()));
        if (Preferences.get(Preferences.USER_PROFILE, "").length() != 0) {
            mUserCollectionDB = UserCollectionDB.get(getActivity());
            mUserWantlistDB = UserWantlistDB.get(getActivity());
        }
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Preferences.get(Preferences.USER_PROFILE, "").length() != 0) {
            try {
                mUserProfileJSON = new JSONObject(Preferences.get(Preferences.USER_PROFILE, ""));
                Log.i("Profile reloaded", "Reloaded " + mUserProfileJSON.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        boolean oauthVerified = checkAccessToken();
        View view;
        if (!oauthVerified) {
            view = inflater.inflate(R.layout.fragment_login_splash, container, false);

            Button signInButton = (Button) view.findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectivityManager = (ConnectivityManager)
                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager.getActiveNetworkInfo() != null
                            && connectivityManager.getActiveNetworkInfo().isAvailable()
                            && connectivityManager.getActiveNetworkInfo().isConnected()) {
                        FetchRequestToken();
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.fragment_dashboard, container, false);
            mUsernameLabel = (TextView) view.findViewById(R.id.user_name_dashboard_label);

            mUserProfilePicture = (ImageView) view.findViewById(R.id.user_profile_picture);

            mCollectionLinearLayout = (LinearLayout) view.findViewById(R.id.collection_dashboard_linear_layout);
            if (mUserCollectionDB != null && mUserCollectionDB.getReleases().size() > 10) {
                for (int i = 0; i < 10; i++) {
                    Release currentRelease = mUserCollectionDB.getReleases().get(i);
                    ImageView imageView = new ImageView(getContext());
                    imageView.setPadding(2, 2, 2, 2);
                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(300, 300);
                    imageView.setLayoutParams(parms);
                    if (currentRelease.getThumbDir().equals("")) {
                        DownloadPreviewThumbnail("Collection", i, imageView);
                    } else {
                        imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                        mCollectionLinearLayout.addView(imageView);
                    }
                }
            }

            mWantlistLinearLayout = (LinearLayout) view.findViewById(R.id.wantlist_dashboard_linear_layout);
            if (mUserWantlistDB != null && mUserWantlistDB.getReleases().size() > 10) {
                for (int i = 0; i < 10; i++) {
                    Release currentRelease = mUserWantlistDB.getReleases().get(i);
                    ImageView imageView = new ImageView(getContext());
                    imageView.setPadding(2, 2, 2, 2);
                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(300, 300);
                    imageView.setLayoutParams(parms);
                    if (currentRelease.getThumbDir().equals("")) {
                        DownloadPreviewThumbnail("Wantlist", i, imageView);
                    } else {
                        imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                        mWantlistLinearLayout.addView(imageView);
                    }
                }
            }
            // Inflate dashboard view
        }
        return view;
    }

    private boolean checkAccessToken() {
        if (Preferences.get(Preferences.OAUTH_ACCESS_KEY, "").length() == 0) {
            return false;
        } else {
            String userIdentityURL = "https://api.discogs.com/oauth/identity";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, userIdentityURL, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            mUserInfoJSON = response;

                            try {
                                String usernameString = mUserInfoJSON.getString("username");
                                Preferences.set(Preferences.USERNAME, usernameString);
                                Log.i("Set Username Pref:", Preferences.get(Preferences.USERNAME, ""));
                                String userIdString = mUserInfoJSON.getString("id");
                                Preferences.set(Preferences.USER_ID, userIdString);
                                Log.i("Set User ID Pref:", Preferences.get(Preferences.USER_ID, ""));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            updateUsername();

                            if (Preferences.get(Preferences.OAUTH_ACCESS_KEY, "").length() != 0 &&
                                    Preferences.get(Preferences.USER_PROFILE, "").length() == 0) {
                                mUserCollectionDB = UserCollectionDB.get(getActivity());
                                mUserWantlistDB = UserWantlistDB.get(getActivity());
                                Toast.makeText(getContext(),
                                        "Logged in as " + Preferences.get(Preferences.USERNAME, "")
                                        , Toast.LENGTH_SHORT)
                                        .show();
                            }

                            if (mUserProfileJSON.length() == 0) {
                                FetchUserProfileJSON();
                            } else {
                                Log.i("User Profile", "Already loaded user");
                                updateProfilePicture();
                                String collectionFirstPageURL = "https://api.discogs.com/users/" +
                                        Preferences.get(Preferences.USERNAME, "") +
                                        "/collection/folders/0/releases?page=1&per_page=100";
                                FetchUserCollectionJSON(collectionFirstPageURL);
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
        return true;
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

    private void setPreviewThumbnails() {
        // Update view with retrieved Collection data
        Release currentRelease;
        for (int i = 0; i < 10; i++) {
            currentRelease = mUserCollectionDB.getReleases().get(i);
            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(2, 2, 2, 2);
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(300, 300);
            imageView.setLayoutParams(parms);

            if (currentRelease.getThumbDir().equals("")) {
                DownloadPreviewThumbnail("Collection", i, imageView);
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                mCollectionLinearLayout.addView(imageView);
            }
        }
        for (int i = 0; i < 10; i++) {
            currentRelease = mUserWantlistDB.getReleases().get(i);
            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(2, 2, 2, 2);
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(300, 300);
            imageView.setLayoutParams(parms);
            if (currentRelease.getThumbDir().equals("")) {
                DownloadPreviewThumbnail("Wantlist", i, imageView);
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                mWantlistLinearLayout.addView(imageView);
            }
        }

    }

    private void updateProfilePicture() {
        String userPictureURL = null;
        try {
            userPictureURL = mUserProfileJSON.getString("avatar_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Instantiate the RequestQueue.
        ImageRequest profilePicRequest = new ImageRequest(userPictureURL,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        mUserProfilePicture.setImageBitmap(response);
                    }
                }, 300, 300, ImageView.ScaleType.FIT_CENTER, null, null);
        // Add the request to the RequestQueue.
        queue.add(profilePicRequest);
    }

    private void updateUsername() {
        mUsernameLabel.setText(Preferences.get(Preferences.USERNAME, ""));
    }

    private void DownloadPreviewThumbnail(final String _thumbDbName, final int dbIndex, final ImageView imageView) {
        long startTime = System.currentTimeMillis();
        String thumbURL = "";
        if (_thumbDbName.equals("Collection")) {
            thumbURL = mUserCollectionDB.getReleases().get(dbIndex).getThumbUrl();
        } else if (_thumbDbName.equals("Wantlist")) {
            thumbURL = mUserWantlistDB.getReleases().get(dbIndex).getThumbUrl();
        }

        // Instantiate the RequestQueue.
        ImageRequest thumbRequest = new ImageRequest(thumbURL,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap releaseCoverBitmap) {
                        try {
                            // path to /data/data/yourapp/app_data/imageDir
                            ContextWrapper cw = new ContextWrapper(getContext());

                            String thumbDir = "";
                            if (_thumbDbName.equals("Collection")) {
                                thumbDir = "CollectionCovers";
                            } else if (_thumbDbName.equals("Wantlist")) {
                                thumbDir = "WantlistCovers";
                            }

                            File directory = cw.getDir(thumbDir, Context.MODE_PRIVATE);
                            // Create imageDir
                            File filePath = new File(directory, "release_cover" + dbIndex + ".jpeg");

                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(filePath);
                                releaseCoverBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (fos != null) {
                                        fos.close();
                                }
                                } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                            Release currentRelease;
                            if (_thumbDbName.equals("Collection")) {
                                currentRelease = mUserCollectionDB.getReleases().get(dbIndex);
                                currentRelease.setThumbDir(filePath.getAbsolutePath());
                                mUserCollectionDB.updateRelease(currentRelease);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                mCollectionLinearLayout.addView(imageView);
                            } else if (_thumbDbName.equals("Wantlist")) {
                                currentRelease = mUserWantlistDB.getReleases().get(dbIndex);
                                currentRelease.setThumbDir(filePath.getAbsolutePath());
                                mUserWantlistDB.updateRelease(currentRelease);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                mWantlistLinearLayout.addView(imageView);
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }, 300, 300, ImageView.ScaleType.FIT_CENTER, null, null);
        // Add the request to the RequestQueue.
        queue.add(thumbRequest);

        // wait for each item
        Log.i("DownloadThumbnail", "Grabbed " + _thumbDbName + " thumbnail file " + dbIndex);
        Log.i("Execution took {}ms", String.valueOf(System.currentTimeMillis() - startTime));
    }

    private void FetchRequestToken() {
        // GET https://api.discogs.com/oauth/request_token
        StringRequest stringRequest = new StringRequest
                (Request.Method.GET, HttpConst.REQUEST_TOKEN_ENDPOINT_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // OnResponse
                        String[] tokenArray = response.split("&");
                        if (tokenArray.length == 3 && tokenArray[0] != null) {
                            OauthVerifyTokens.setOauthRequestTokenSecret(tokenArray[0].split("=")[1]);
                            OauthVerifyTokens.setOauthRequestToken(tokenArray[1].split("=")[1]);

                            String authUrl = null;
                            if (OauthVerifyTokens.getOauthRequestToken() != null) {
                                authUrl = HttpConst.AUTHORIZATION_WEBSITE_URL + "?oauth_token=" +
                                        OauthVerifyTokens.getOauthRequestToken();
                                Log.i("Auth URL", authUrl);
                            } else {
                                Log.i("Auth Dialog", "No oauth request token values populated");
                            }

                            if (authUrl != null) {
                                Uri authUri = Uri.parse(authUrl);
                                Intent i = AuthPageActivity.newIntent(getActivity(), authUri);
                                startActivity(i);
                            }
                        }

                }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        // Read POST response code
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
                        ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts +
                        ", oauth_callback=" + HttpConst.CALLBACK_URL);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(stringRequest);
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
                        updateProfilePicture();
                        String collectionFirstPageURL = "https://api.discogs.com/users/" +
                                Preferences.get(Preferences.USERNAME, "") +
                                "/collection/folders/0/releases?page=1&per_page=100";
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
                                        "/wants?page=1&per_page=100";
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
                                setPreviewThumbnails();
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
