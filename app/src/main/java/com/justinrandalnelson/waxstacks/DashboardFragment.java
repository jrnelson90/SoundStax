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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private JSONObject mUserProfileJSON = new JSONObject();
    private LinearLayout mCollectionLinearLayout;
    private LinearLayout mWantlistLinearLayout;
    private TextView mUsernameLabel;
    private ImageView mUserProfilePicture;
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
            if (mCollectionLinearLayout.getChildCount() != 0) {
                mCollectionLinearLayout.removeAllViews();
            }
            if (mUserCollectionDB != null && mUserCollectionDB.getReleases().size() > 10) {
                for (int i = 0; i < 10; i++) {
                    final Release currentRelease = mUserCollectionDB.getReleases().get(i);
                    ImageView imageView = new ImageView(getContext());
                    imageView.setPadding(2, 2, 2, 2);
                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(300, 300);
                    imageView.setLayoutParams(parms);
                    if (currentRelease.getThumbDir().equals("")) {
                        DownloadPreviewThumbnail("Collection", i, imageView);
                    } else {
                        imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = ReleaseActivity.newIntent(getActivity(),
                                        currentRelease.getId(), "Collection");
                                startActivity(intent);
                            }
                        });
                        mCollectionLinearLayout.addView(imageView);
                    }
                }
            }

            mWantlistLinearLayout = (LinearLayout) view.findViewById(R.id.wantlist_dashboard_linear_layout);
            if (mWantlistLinearLayout.getChildCount() != 0) {
                mWantlistLinearLayout.removeAllViews();
            }
            if (mUserWantlistDB != null && mUserWantlistDB.getReleases().size() > 10) {
                for (int i = 0; i < 10; i++) {
                    final Release currentRelease = mUserWantlistDB.getReleases().get(i);
                    ImageView imageView = new ImageView(getContext());
                    imageView.setPadding(2, 2, 2, 2);
                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(300, 300);
                    imageView.setLayoutParams(parms);
                    if (currentRelease.getThumbDir().equals("")) {
                        DownloadPreviewThumbnail("Wantlist", i, imageView);
                    } else {
                        imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = ReleaseActivity.newIntent(getActivity(),
                                        currentRelease.getId(), "Wantlist");
                                startActivity(intent);
                            }
                        });
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
                            updateUsername();
                            Log.i("User Profile", "Already loaded user");
                            updateProfilePicture();
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

    private void setPreviewThumbnails() {
        // Update view with retrieved Collection data
        Release currentRelease;
        if (mCollectionLinearLayout.getChildCount() != 0) {
            mCollectionLinearLayout.removeAllViews();
        }
        if (mWantlistLinearLayout.getChildCount() != 0) {
            mWantlistLinearLayout.removeAllViews();
        }
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
                final Release finalCurrentRelease = currentRelease;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = ReleaseActivity.newIntent(getActivity(),
                                finalCurrentRelease.getId(), "Collection");
                        startActivity(intent);
                    }
                });
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
                final Release finalCurrentRelease = currentRelease;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = ReleaseActivity.newIntent(getActivity(),
                                finalCurrentRelease.getId(), "Wantlist");
                        startActivity(intent);
                    }
                });
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
                        setPreviewThumbnails();
                    }
                }, 300, 300, ImageView.ScaleType.FIT_CENTER, null, null);
        // Add the request to the RequestQueue.
        queue.add(profilePicRequest);
    }

    private void updateUsername() {
        mUsernameLabel.setText(Preferences.get(Preferences.USERNAME, ""));
    }

    private void DownloadPreviewThumbnail(final String _thumbDbName, final int dbIndex,
                                          final ImageView imageView) {
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
                            final Release currentRelease;
                            if (_thumbDbName.equals("Collection")) {
                                currentRelease = mUserCollectionDB.getReleases().get(dbIndex);
                                currentRelease.setThumbDir(filePath.getAbsolutePath());
                                mUserCollectionDB.updateRelease(currentRelease);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = ReleaseActivity.newIntent(getActivity(),
                                                currentRelease.getId(), "Collection");
                                        startActivity(intent);
                                    }
                                });
                                mCollectionLinearLayout.addView(imageView);
                            } else if (_thumbDbName.equals("Wantlist")) {
                                currentRelease = mUserWantlistDB.getReleases().get(dbIndex);
                                currentRelease.setThumbDir(filePath.getAbsolutePath());
                                mUserWantlistDB.updateRelease(currentRelease);
                                imageView.setImageBitmap(BitmapFactory.decodeFile(currentRelease.getThumbDir()));
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = ReleaseActivity.newIntent(getActivity(),
                                                currentRelease.getId(), "Wantlist");
                                        startActivity(intent);
                                    }
                                });
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

}
