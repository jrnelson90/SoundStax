package com.justinrandalnelson.waxstacks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Release Fragment View - Shows details about a particular music item release
 * Created by jrnel on 2/18/2017.
 */

public class ReleaseFragment extends Fragment {
    private static final String ARG_RELEASE_ID = "release_id";
    private static final String ARG_RELEASE = "release";
    private static String parentList;
    private Release mRelease;
    private TextView mTitleField;
    private ImageView mReleaseCoverView;
    private JSONObject mReleaseJSON;
    private RequestQueue queue;

    public static ReleaseFragment newInstance(UUID releaseID, String _parentList) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RELEASE_ID, releaseID);
        final ReleaseFragment fragment = new ReleaseFragment();
        fragment.setArguments(args);
        parentList = _parentList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID releaseID;
        if (parentList.equals("Collection")) {
            releaseID = (UUID) getArguments().getSerializable(ARG_RELEASE_ID);
            mRelease = UserCollectionDB.get(getActivity()).getRelease(releaseID);
        } else if (parentList.equals("Wantlist")) {
            releaseID = (UUID) getArguments().getSerializable(ARG_RELEASE_ID);
            mRelease = UserWantlistDB.get(getActivity()).getRelease(releaseID);
        } else if (parentList.equals("Search")) {
            Bundle args = getActivity().getIntent().getExtras();
            mRelease = (Release) args.getSerializable(ARG_RELEASE);
        }
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        String releaseURL = "https://api.discogs.com/releases/" + mRelease.getReleaseId();
        JsonObjectRequest releaseJSON = new JsonObjectRequest(Request.Method.GET, releaseURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mReleaseJSON = response;
                        loadReleasePicture();
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
        queue.add(releaseJSON);
    }

    private void loadReleasePicture() {
        String releaseCoverLargeUrl = "";
        try {
            releaseCoverLargeUrl = (String) mReleaseJSON.getJSONArray("images").getJSONObject(0).get("uri");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ImageRequest thumbRequest = new ImageRequest(releaseCoverLargeUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap releaseCoverBitmap) {
                        mReleaseCoverView.setImageBitmap(releaseCoverBitmap);
                    }
                }, 600, 600, ImageView.ScaleType.FIT_CENTER, null, null);
        // Add the request to the RequestQueue.
        queue.add(thumbRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_release_page, container, false);

        mReleaseCoverView = (ImageView) v.findViewById(R.id.release_cover_image_view);

        File imgFile = new File(mRelease.getThumbDir());
        if (imgFile.exists()) {
            Bitmap coverBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mReleaseCoverView.setImageBitmap(coverBitmap);
        }

        mTitleField = (TextView) v.findViewById(R.id.release_title);
        mTitleField.setText(mRelease.getTitle());

        TextView artistField = (TextView) v.findViewById(R.id.release_artist);
        artistField.setText(mRelease.getArtist());

        TextView genreField = (TextView) v.findViewById(R.id.release_genre);
//        if(mRelease.getGenre().equals("")){
        String empty = "(None Specified)";
        genreField.setText(empty);
//        } else{
//            genreField.setText(mRelease.getGenre());
//        }

        TextView yearField = (TextView) v.findViewById(R.id.release_year);
        yearField.setText(mRelease.getYear());

        final Button mModifyListActionOneButton = (Button) v.findViewById(R.id.modify_release_one_button);
        final Button mModifyListActionTwoButton = (Button) v.findViewById(R.id.modify_release_two_button);
        String removeButtonText;
        switch (parentList) {
            case "Collection":
                removeButtonText = "Remove from " + parentList;
                mModifyListActionOneButton.setText(removeButtonText);
                mModifyListActionTwoButton.setVisibility(View.INVISIBLE);
                mModifyListActionOneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), mTitleField.getText().toString() + " deleted.",
                                Toast.LENGTH_SHORT).show();
                        String releaseURL = "https://api.discogs.com/users/" +
                                Preferences.get(Preferences.USERNAME, "") + "/collection/folders/0/releases/" +
                                mRelease.getReleaseId() + "/instances/" + mRelease.getInstanceId();
                        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, releaseURL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                            }
                        })

                        {
                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                int mStatusCode = response.statusCode;
                                if (mStatusCode == 204) {
                                    UserCollectionDB.get(getActivity()).deleteRelease(mRelease);
                                    getActivity().finish();
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
                        queue.add(stringRequest);
                    }

                });
                break;
            case "Wantlist":
                removeButtonText = "Remove from " + parentList;
                mModifyListActionOneButton.setText(removeButtonText);
                mModifyListActionTwoButton.setVisibility(View.INVISIBLE);
                mModifyListActionOneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), mTitleField.getText().toString() + " deleted.",
                                Toast.LENGTH_SHORT).show();
                        String releaseURL = "https://api.discogs.com/users/" +
                                Preferences.get(Preferences.USERNAME, "") + "/wants/" + mRelease.getReleaseId();
                        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, releaseURL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                            }
                        })

                        {
                            @Override
                            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                                int mStatusCode = response.statusCode;
                                if (mStatusCode == 204) {
                                    UserWantlistDB.get(getActivity()).deleteRelease(mRelease);
                                    getActivity().finish();
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
                        queue.add(stringRequest);
                    }

                });
                break;
            case "Search":
                final String collectionButtonText = "Add to Collection";
                String wantlistButtonText = "Add to Wantlist";
                mModifyListActionOneButton.setText(collectionButtonText);
                mModifyListActionTwoButton.setText(wantlistButtonText);
                mModifyListActionOneButton.setOnClickListener(new View.OnClickListener() {
                    int mStatusCode;

                    @Override
                    public void onClick(View view) {

                        String releaseURL = "https://api.discogs.com/users/" +
                                Preferences.get(Preferences.USERNAME, "") +
                                "/collection/folders/1/releases/" + mRelease.getReleaseId();
                        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, releaseURL, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        if (mStatusCode == 201) {
                                            String instanceId = null;
                                            String dateAdded = null;
                                            try {
                                                instanceId = response.getString("instance_id");
                                                mRelease.setInstanceId(instanceId);
//                                                dateAdded = response.getString("date_added");
                                                //       "date_added": "2015-11-30T10:54:13-08:00",
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
                                                Date now = new Date();
                                                String strDate = sdf.format(now);
                                                mRelease.setDateAdded(strDate);
                                                UserCollectionDB.get(getActivity()).addRelease(mRelease);
                                                Toast.makeText(getActivity(), mTitleField.getText().toString()
                                                                + " added to Collection.",
                                                        Toast.LENGTH_SHORT).show();
                                                mModifyListActionOneButton.setActivated(false);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            // getActivity().finish();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                            }
                        })

                        {
                            @Override
                            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                                mStatusCode = response.statusCode;
                                return super.parseNetworkResponse(response);
                            }

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
                        queue.add(stringRequest);
                    }

                });
//
                mModifyListActionTwoButton.setOnClickListener(new View.OnClickListener() {
                    int mStatusCode;

                    @Override
                    public void onClick(View view) {

                        String releaseURL = "https://api.discogs.com/users/" +
                                Preferences.get(Preferences.USERNAME, "") +
                                "/wants/" + mRelease.getReleaseId();
                        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.PUT, releaseURL, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        if (mStatusCode == 201) {
                                            UserWantlistDB.get(getActivity()).addRelease(mRelease);
                                            Toast.makeText(getActivity(), mTitleField.getText().toString()
                                                            + " added to Wantlist.",
                                                    Toast.LENGTH_SHORT).show();
                                            mModifyListActionTwoButton.setActivated(false);
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                            }
                        })

                        {
                            @Override
                            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                                mStatusCode = response.statusCode;
                                return super.parseNetworkResponse(response);
                            }

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
                        queue.add(stringRequest);
                    }

                });
                break;
        }

        return v;
    }
}