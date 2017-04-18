package com.justinrandalnelson.waxstacks;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Dashboard fragment
 * Created by jrnel on 4/14/2017.
 */

public class DashboardFragment extends Fragment {
    private UserCollectionDB mUserCollectionDB;
    private UserWantlistDB mUserWantlistDB;
    private JSONObject mUserInfoJSON = new JSONObject();
    private JSONObject mUserCollectionJSON = new JSONObject();
    private JSONObject mUserWantlistJSON = new JSONObject();
    private JSONObject mUserProfileJSON = new JSONObject();
    private LinearLayout mCollectionLinearLayout;
    private LinearLayout mWantlistLinearLayout;
    private TextView mUsernameLabel;
    private ImageView mUserProfilePicture;
    private ArrayList<Bitmap> mReleaseBitmaps = new ArrayList<>();
    private ArrayList<ImageView> mCollectionPreview = new ArrayList<>();
    private ArrayList<ImageView> mWantlistPreview = new ArrayList<>();
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
//        Bundle bundle = new Bundle();
//        bundle.putString(JSON_STRING,json.toString());

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
                        new FetchRequestToken().execute();
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.fragment_dashboard, container, false);
            mUsernameLabel = (TextView) view.findViewById(R.id.user_name_dashboard_label);

            mUserProfilePicture = (ImageView) view.findViewById(R.id.user_profile_picture);

            mCollectionLinearLayout = (LinearLayout) view.findViewById(R.id.collection_dashboard_linear_layout);
//            if(mUserCollectionDB.getReleases().size() == 0) {
            for (int i = 0; i < 10; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setId(i);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.disc_vinyl_icon));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mCollectionPreview.add(imageView);
                mCollectionLinearLayout.addView(imageView);
            }

            mWantlistLinearLayout = (LinearLayout) view.findViewById(R.id.wantlist_dashboard_linear_layout);
            for (int i = 0; i < 10; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setId(i);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.disc_vinyl_icon));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mWantlistPreview.add(imageView);
                mWantlistLinearLayout.addView(imageView);
            }
            // Inflate dashboard view
        }
        return view;
    }

    private boolean checkAccessToken() {
        if (Preferences.get(Preferences.OAUTH_ACCESS_KEY, "").length() == 0) {
            return false;
        } else {
            FetchUserIdentityJSON checkUser = new FetchUserIdentityJSON();
            checkUser.execute();
        }
        return true;
    }

    private void extractCollectionData() {
        try {
            JSONArray UserCollectionArray = (JSONArray) mUserCollectionJSON.get("releases");
            Log.i("Collection Parse", "ReleaseGSON array created");

            if (UserCollectionArray.length() != mUserCollectionDB.getReleases().size()) {
                for (int i = 0; i < UserCollectionArray.length(); i++) {
                    JSONObject currentRelease = (JSONObject) UserCollectionArray.get(i);
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
            JSONArray UserWantlistArray = (JSONArray) mUserWantlistJSON.get("wants");
            Log.i("Wantlist Parse", "ReleaseGSON array created");

            if (UserWantlistArray.length() != mUserWantlistDB.getReleases().size()) {
                for (int i = 0; i < UserWantlistArray.length(); i++) {
                    JSONObject currentRelease = (JSONObject) UserWantlistArray.get(i);
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

    private void downloadListThumbnails() {
        // Update view with retrieved Collection data
        if (mUserCollectionDB.getReleases().get(0).getThumbDir().equals("")) {
            ThumbDownloader("Collection");
        }
        if (mUserWantlistDB.getReleases().get(0).getThumbDir().equals("")) {
            ThumbDownloader("Wantlist");
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
                }, 200, 200, ImageView.ScaleType.FIT_CENTER, null, null);
        // Add the request to the RequestQueue.
        queue.add(profilePicRequest);
    }

    private void updateUsername() {
        mUsernameLabel.setText(Preferences.get(Preferences.USERNAME, ""));
    }

    private void ThumbDownloader(final String _thumbDbName) {
        final String TAG = "ThumbDownloader";

        int downloadListSize = 0;
        if (_thumbDbName.equals("Collection")) {
            downloadListSize = mUserCollectionDB.getReleases().size();
        } else if (_thumbDbName.equals("Wantlist")) {
            downloadListSize = mUserWantlistDB.getReleases().size();
        }
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < downloadListSize; i++) {
            String thumbURL = "";
            if (_thumbDbName.equals("Collection")) {
                thumbURL = mUserCollectionDB.getReleases().get(i).getThumbUrl();
            } else if (_thumbDbName.equals("Wantlist")) {
                thumbURL = mUserWantlistDB.getReleases().get(i).getThumbUrl();
            }

            // Instantiate the RequestQueue.
            final int finalI = i;
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
                                File filePath = new File(directory, "release_cover" + finalI + ".jpeg");

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
                                Release currentRelease = null;
                                if (_thumbDbName.equals("Collection")) {
                                    currentRelease = mUserCollectionDB.getReleases().get(finalI);
                                    currentRelease.setThumbDir(filePath.getAbsolutePath());
                                    mUserCollectionDB.updateRelease(currentRelease);
                                } else if (_thumbDbName.equals("Wantlist")) {
                                    currentRelease = mUserWantlistDB.getReleases().get(finalI);
                                    currentRelease.setThumbDir(filePath.getAbsolutePath());
                                    mUserWantlistDB.updateRelease(currentRelease);
                                }
//                                if (currentRelease != null) {
//                                    Log.i(TAG, currentRelease.getThumbDir());
//                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 200, 200, ImageView.ScaleType.FIT_CENTER, null, null);
            // Add the request to the RequestQueue.
            queue.add(thumbRequest);


        }
        // wait for each item
        Log.i("ThumbDownloader", "Grabbed all " + _thumbDbName + " thumbnail files");
        Log.i("Execution took {}ms", String.valueOf(System.currentTimeMillis() - startTime));

    }

    private class FetchRequestToken extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            return new OauthTokenFetcher().fetchRequestToken();
        }

        @Override
        protected void onPostExecute(String[] tokenArray) {
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
    }

    private class FetchUserIdentityJSON extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            return new JsonFetcher().fetchUserIdentity();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mUserInfoJSON = jsonObject;

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
                new FetchUserProfileJSON().execute();
            } else {
                Log.i("User Profile", "Already loaded user");
                updateProfilePicture();
                new FetchUserCollectionJSON().execute();
            }
        }
    }

//    private class FetchUserProilePicture extends AsyncTask<Void, Void, Bitmap> {
//        @Override
//        protected Bitmap doInBackground(Void... params) {
//            String profilePicUrl = null;
//            try {
//                profilePicUrl = mUserProfileJSON.getString("avatar_url");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return new JsonFetcher().fetchUserProfilePicture(profilePicUrl);
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap _userProfilePicBitmap) {
////            updateProfilePicture(_userProfilePicBitmap);
////            new FetchUserCollectionJSON().execute();
//        }
//    }

    private class FetchUserProfileJSON extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            return new JsonFetcher().fetchUserProfile();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mUserProfileJSON = jsonObject;
            Preferences.set(Preferences.USER_PROFILE, mUserProfileJSON.toString());
            updateProfilePicture();
            new FetchUserCollectionJSON().execute();
        }
    }

    private class FetchUserCollectionJSON extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            return new JsonFetcher().fetchUserCollection();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mUserCollectionJSON = jsonObject;
            new FetchUserWantlistJSON().execute();
        }
    }

    private class FetchUserWantlistJSON extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            return new JsonFetcher().fetchUserWantlist();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mUserWantlistJSON = jsonObject;
            extractCollectionData();
            extractWantlistData();
            downloadListThumbnails();
        }
    }

}
