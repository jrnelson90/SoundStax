package com.justinrandalnelson.waxstacks;

import android.content.Context;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Dashboard fragment
 * Created by jrnel on 4/14/2017.
 */

public class DashboardFragment extends Fragment {
    private JSONObject mUserInfoJSON = new JSONObject();
    private JSONObject mUserCollectionJSON = new JSONObject();
    private JSONObject mUserWantlistJSON = new JSONObject();
    private JSONObject mUserProfileJSON = new JSONObject();
    private LinearLayout mCollectionLinearLayout;
    private LinearLayout mWantlistLinearLayout;
    private TextView mUsernameLabel;
    private ImageView mUserProfilePicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Preferences.setPreferenceContext(PreferenceManager.getDefaultSharedPreferences(getContext()));
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
            for (int i = 0; i < 10; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setId(i);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.disc_vinyl_icon));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
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
            mUserCollectionJSON.get("release");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractWantlistData() {
        try {
            mUserWantlistJSON.get("release");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateDashboardCollectionView() {
        // Update view with retrieved Collection data
    }

    private void updateProfilePicture() {
        String userPictureURL = null;
        try {
            userPictureURL = mUserProfileJSON.getString("avatar_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Glide.with(getContext())
                .load(userPictureURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .fitCenter()
                .placeholder(R.drawable.loading)
                .into(mUserProfilePicture);
    }

    private void updateUsername() {
        mUsernameLabel.setText(Preferences.get(Preferences.USERNAME, ""));
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

    private class FetchUserCollectionJSON extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            return new JsonFetcher().fetchUserCollection();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mUserCollectionJSON = jsonObject;
//            extractCollectionData();
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
//            extractWantlistData();
            populateDashboardCollectionView();
        }
    }
}
