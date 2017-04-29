package com.soundstax.soundstax;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
        setHasOptionsMenu(true);
        if (Preferences.get(Preferences.USER_PROFILE, "").length() != 0) {
            mUserCollectionDB = UserCollectionDB.get(getActivity());
            mUserWantlistDB = UserWantlistDB.get(getActivity());
            try {
                mUserProfileJSON = new JSONObject(Preferences.get(Preferences.USER_PROFILE, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Preferences.get(Preferences.USER_PROFILE, "").length() != 0) {
            setPreviewThumbnails();
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
        View view = null;
        if (oauthVerified) {
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
                        DownloadPreviewThumbnail("Collection", i, currentRelease.getReleaseId(), imageView);
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
                        DownloadPreviewThumbnail("Wantlist", i, currentRelease.getReleaseId(), imageView);
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
            updateProfilePicture();
        } else {
            Preferences.set(Preferences.OAUTH_ACCESS_KEY, "");
            Preferences.set(Preferences.OAUTH_ACCESS_SECRET, "");
            Preferences.set(Preferences.USERNAME, "");
            Preferences.set(Preferences.USER_ID, "");
            Preferences.set(Preferences.USER_PROFILE, "");
            Preferences.set(Preferences.USER_PIC_DIR, "");

            File collectionImageDir =
                    new File("/data/user/0/com.soundstax.soundstax/app_CollectionCovers");
            if (collectionImageDir.isDirectory()) {
                String[] children = collectionImageDir.list();
                for (String aChildren : children) {
                    File currentImage = new File(collectionImageDir, aChildren);
                    currentImage.delete();
                }
            }
            File wantlistImageDir =
                    new File("/data/user/0/com.soundstax.soundstax/app_WantlistCovers");
            if (wantlistImageDir.isDirectory()) {
                String[] children = wantlistImageDir.list();
                for (String aChildren : children) {
                    File currentImage = new File(collectionImageDir, aChildren);
                    currentImage.delete();
                }
            }
            mUserCollectionDB.deleteAllReleases();
            mUserWantlistDB.deleteAllReleases();
            Intent i = new Intent(getActivity(), LoginSplashActivity.class);
            startActivity(i);
            getActivity().finish();
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_toolbar_layout, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // your text view here
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setFocusable(false);
                searchItem.collapseActionView();
                Intent i = new Intent(getActivity(), SearchResultsActivity.class);
                Bundle args = new Bundle();
                args.putString("query", query);
                i.putExtras(args);
                startActivity(i);
                return true;
            }
        });
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
                            updateUsername();
                            Log.i("User Profile", "Already loaded user");
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
                DownloadPreviewThumbnail("Collection", i, currentRelease.getReleaseId(), imageView);
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
                DownloadPreviewThumbnail("Wantlist", i, currentRelease.getReleaseId(), imageView);
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
        if (Preferences.get(Preferences.USER_PIC_DIR, "").length() == 0) {
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
                            // path to /data/data/yourapp/app_data/imageDir
                            ContextWrapper cw = new ContextWrapper(getContext());
                            String thumbDir = "ProfilePicture";
                            File directory = cw.getDir(thumbDir, Context.MODE_PRIVATE);
                            // Create imageDir
                            File filePath = null;
                            try {
                                filePath = new File(directory, mUserProfileJSON.getString("id") + ".jpeg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            FileOutputStream fos = null;
                            try {
                                assert filePath != null;
                                fos = new FileOutputStream(filePath);
                                response.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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
                            Preferences.set(Preferences.USER_PIC_DIR, filePath.getAbsolutePath());
                            mUserProfilePicture.setImageBitmap(BitmapFactory.decodeFile(
                                    Preferences.get(Preferences.USER_PIC_DIR, "")));
                            setPreviewThumbnails();
                        }
                    }, 300, 300, ImageView.ScaleType.FIT_CENTER, null, null);
            // Add the request to the RequestQueue.
            queue.add(profilePicRequest);
        } else {
            mUserProfilePicture.setImageBitmap(BitmapFactory.decodeFile(
                    Preferences.get(Preferences.USER_PIC_DIR, "")));
            setPreviewThumbnails();
        }

    }

    private void updateUsername() {
        mUsernameLabel.setText(Preferences.get(Preferences.USERNAME, ""));
    }

    private void DownloadPreviewThumbnail(final String _thumbDbName, final int dbIndex,
                                          final String _releaseID, final ImageView imageView) {
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
                            File filePath = new File(directory, "release_" + _releaseID + "_cover.jpeg");

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


}
