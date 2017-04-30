package com.soundstax.soundstax;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static boolean loadedReleaseInfo;
    private static boolean loadedHighRes;
    private Release mRelease;
    private TextView mTitleField;
    private ImageView mReleaseCoverView;
    private JSONObject mReleaseJSON;
    private RequestQueue queue;
    private JSONObject mSpotifyReleaseInfo;
    private Button mSpotifyButton;
    private TextView mReleaseFormatInfo;
    private TextView mReleaseLabels;
    private android.widget.TableLayout mTrackInfoTable;
    private TextView mReleaseGenre;

    public static ReleaseFragment newInstance(UUID releaseID, String _parentList) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RELEASE_ID, releaseID);
        final ReleaseFragment fragment = new ReleaseFragment();
        fragment.setArguments(args);
        parentList = _parentList;
        loadedReleaseInfo = false;
        loadedHighRes = false;
        return fragment;
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        for (String item : items) {
            if (inputStr.contains(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID releaseID;
        switch (parentList) {
            case "Collection":
                releaseID = (UUID) getArguments().getSerializable(ARG_RELEASE_ID);
                mRelease = UserCollectionDB.get(getActivity()).getRelease(releaseID);
                break;
            case "Wantlist":
                releaseID = (UUID) getArguments().getSerializable(ARG_RELEASE_ID);
                mRelease = UserWantlistDB.get(getActivity()).getRelease(releaseID);
                break;
            case "Search":
                Bundle args = getActivity().getIntent().getExtras();
                mRelease = (Release) args.getSerializable(ARG_RELEASE);
                break;
        }
        queue = VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!loadedReleaseInfo) {
            String releaseURL = "https://api.discogs.com/releases/" + mRelease.getReleaseId();
            JsonObjectRequest releaseJSON = new JsonObjectRequest(Request.Method.GET, releaseURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            mReleaseJSON = response;
                            loadedReleaseInfo = true;
                            getSpotifyLink();
                            loadReleasePicture();
                            loadReleaseInfo();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

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
            queue.add(releaseJSON);
        }
    }

    private void loadReleaseInfo() {
        // TODO: Create method for parsing track info
        String releaseGenre = null;
        String releaseLabel = null;
        try {
            releaseGenre = mReleaseJSON.getJSONArray("styles").getString(0);
            mReleaseGenre.setText(releaseGenre);
            releaseLabel = mReleaseJSON.getJSONArray("labels").getJSONObject(0).getString("name");
            mReleaseLabels.setText(releaseLabel);
            JSONArray tracklist = mReleaseJSON.getJSONArray("tracklist");

            TableRow trackColumnTitleRow = new TableRow(getContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            trackColumnTitleRow.setLayoutParams(lp);

            int dpInPx = (int) (8 * Resources.getSystem().getDisplayMetrics().density);
            TextView trackNumberLabel = new TextView(getContext());
            TextView trackNameLabel = new TextView(getContext());
            TextView trackDurationLabel = new TextView(getContext());

            String trackNumberLabelString = "Track #";
            trackNumberLabel.setText(trackNumberLabelString);

            trackNameLabel.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f));
            String trackNameLabelString = "Title";
            trackNameLabel.setText(trackNameLabelString);
            trackNameLabel.setSingleLine(true);
            trackNameLabel.setEllipsize(TextUtils.TruncateAt.END);
            trackNameLabel.setPadding(dpInPx, 0, 0, 0);
            trackNameLabel.setHorizontallyScrolling(false);

            String trackDurationLabelString = "Length";
            trackDurationLabel.setText(trackDurationLabelString);

            trackColumnTitleRow.addView(trackNumberLabel);
            trackColumnTitleRow.addView(trackNameLabel);
            trackColumnTitleRow.addView(trackDurationLabel);
            mTrackInfoTable.addView(trackColumnTitleRow, 0);
            int trackCounter = 0;
            for (int i = 0; i < tracklist.length(); i++) {
                JSONObject currentTrack = tracklist.getJSONObject(i);
                if (currentTrack.getString("type_").equals("track")) {
                    TableRow trackRow = new TableRow(getContext());
                    trackRow.setLayoutParams(lp);

                    TextView trackNumber = new TextView(getContext());
                    TextView trackName = new TextView(getContext());
                    TextView trackDuration = new TextView(getContext());

                    String trackNumberString = currentTrack.getString("position");
                    trackNumber.setText(trackNumberString);

                    String trackNameString = currentTrack.getString("title");
                    trackName.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT,
                            1f));
                    trackName.setText(trackNameString);
                    trackName.setSingleLine(true);
                    trackName.setEllipsize(TextUtils.TruncateAt.END);
                    trackName.setHorizontallyScrolling(false);
                    trackName.setPadding(dpInPx, 0, dpInPx, 0);

                    String trackDurationString = currentTrack.getString("duration");
                    if (trackDurationString.length() == 0) {
                        trackDurationString = "N/A";
                    }
                    trackDuration.setText(trackDurationString);

                    trackRow.addView(trackNumber);
                    trackRow.addView(trackName);
                    trackRow.addView(trackDuration);
                    mTrackInfoTable.addView(trackRow, trackCounter++ + 1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        mReleaseUserFolder ;
    }

    private void loadReleasePicture() {
        if (!loadedHighRes) {
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

                            try {
                                FileOutputStream fos = null;
                                File imgFile = new File(mRelease.getThumbDir());
                                try {
                                    fos = new FileOutputStream(imgFile);
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

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            loadedHighRes = true;
                        }
                    }, 600, 600, ImageView.ScaleType.FIT_CENTER, null, null);
            // Add the request to the RequestQueue.
            queue.add(thumbRequest);
        }
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
        mTrackInfoTable = (TableLayout) v.findViewById(R.id.track_info_table);
        mReleaseLabels = (TextView) v.findViewById(R.id.release_labels);

        mReleaseFormatInfo = (TextView) v.findViewById(R.id.release_format_info);
        mReleaseFormatInfo.setText(mRelease.getFormatName());
        String formatInfoParsed = "";
        for (int i = 0; i < mRelease.getFormatDescriptionsArray().length; i++) {
            formatInfoParsed += mRelease.getFormatDescriptionsArray()[i];
            if (mRelease.getFormatDescriptionsArray().length >= 2 &&
                    i != mRelease.getFormatDescriptionsArray().length - 1) {
                formatInfoParsed += " ";
            }
        }
        mReleaseFormatInfo.append(" (" + formatInfoParsed);
        if (mRelease.getFormatText() != null && mRelease.getFormatText().length() > 0) {
            mReleaseFormatInfo.append(" " + mRelease.getFormatText() + ")");
        } else {
            mReleaseFormatInfo.append(")");
        }

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

        mReleaseGenre = (TextView) v.findViewById(R.id.release_genre);
//        if(mRelease.getGenre().equals("")){
//        String empty = "(None Specified)";
//        mReleaseGenre.setText(empty);
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
                mModifyListActionTwoButton.setVisibility(View.GONE);
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
                        queue.add(stringRequest);
                    }

                });
                break;
            case "Wantlist":
                removeButtonText = "Remove from " + parentList;
                mModifyListActionOneButton.setText(removeButtonText);
                mModifyListActionTwoButton.setVisibility(View.GONE);
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
                                            String instanceId;
                                            try {
                                                instanceId = response.getString("instance_id");
                                                mRelease.setInstanceId(instanceId);
//                                                dateAdded = response.getString("date_added");
                                                //       "date_added": "2015-11-30T10:54:13-08:00",
                                                SimpleDateFormat sdf =
                                                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
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
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-SSS");
                                            Date now = new Date();
                                            String strDate = sdf.format(now);
                                            mRelease.setDateAdded(strDate);
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
                        queue.add(stringRequest);
                    }

                });
                break;
        }

        mSpotifyButton = (Button) v.findViewById(R.id.spotify_play_button);
        mSpotifyButton.setVisibility(View.GONE);

        return v;
    }

    private void getSpotifyLink() {

        // https://api.spotify.com/v1/search?q=album:arrival%20artist:abba&type=album
        final String parsedArtist = mRelease.getArtist().split(" \\(")[0].toLowerCase()
                .replace("and ", "").replace("& ", "");
        String releaseTitle = mRelease.getTitle().toLowerCase().trim().replace("-", " ")
                .replaceAll("[+.^:,()\\[\\]\"]", "").replace("   ", "  ").replace("  ", " ");
        if (stringContainsItemFromList(releaseTitle, new String[]{"original", "motion", "picture",
                "movie", "cast", "recording", "broadway", "soundtrack"})) {
            String releaseURL = "https://api.spotify.com/v1/search?q=album:" +
                    Uri.encode(releaseTitle) + "&type=album";
            int mStatusCode = 0;
            final int[] finalMStatusCode = {mStatusCode};
            final String finalReleaseTitle1 = releaseTitle;
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, releaseURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (finalMStatusCode[0] == 200) {
                                mSpotifyReleaseInfo = response;
                                try {
                                    JSONObject albums = mSpotifyReleaseInfo.getJSONObject("albums");
                                    JSONArray items = albums.getJSONArray("items");
                                    if (items.length() > 0) {
                                        for (int i = 0; i < items.length(); i++) {
                                            JSONObject currentResult = (JSONObject) items.get(i);
                                            String title = currentResult.getString("name").toLowerCase()
                                                    .replaceAll("[-+.^:,()\\[\\]\"]", "")
                                                    .replace("remastered", "").replace("version", "")
                                                    .replace("edition", "").replace("  ", " ");
                                            if (title.replace(" ", "").equals(finalReleaseTitle1.replace(" ", ""))) {

                                                String albumURI = currentResult.getString("uri");
                                                setSpotifyButton(albumURI);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            })

            {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    finalMStatusCode[0] = response.statusCode;
                    return super.parseNetworkResponse(response);
                }
            };
            queue.add(stringRequest);
        } else {
            releaseTitle = releaseTitle.split("\\(")[0].split(":")[0];
            String releaseURL = "https://api.spotify.com/v1/search?q=album:" +
                    Uri.encode(releaseTitle) + "+artist:" + Uri.encode(parsedArtist) + "&type=album";
            int mStatusCode = 0;
            final int[] finalMStatusCode = {mStatusCode};
            final String finalReleaseTitle = releaseTitle;
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, releaseURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (finalMStatusCode[0] == 200) {
                                mSpotifyReleaseInfo = response;
                                try {
                                    JSONObject albums = mSpotifyReleaseInfo.getJSONObject("albums");
                                    JSONArray items = albums.getJSONArray("items");
                                    if (items.length() > 0) {
                                        for (int i = 0; i < items.length(); i++) {
                                            JSONObject currentResult = (JSONObject) items.get(i);
                                            String title = currentResult.getString("name");
                                            title = title.split(" \\(")[0].split(":")[0]
                                                    .toLowerCase().replaceAll("[+.^:,()\\[\\]\"]", "")
                                                    .replace("remastered", "").replace("version", "")
                                                    .replace("edition", "").replace("  ", " ");

                                            JSONArray artistsArray = currentResult.getJSONArray("artists");
                                            if (artistsArray.length() > 0) {
                                                for (int j = 0; j < artistsArray.length(); j++) {
                                                    JSONObject currentArtist = (JSONObject) artistsArray.get(j);
                                                    String artistName = currentArtist.getString("name")
                                                            .toLowerCase().replace(".", "")
                                                            .replace("and ", "").replace("& ", "");
                                                    if (artistName.equals("various artists")) {
                                                        artistName = "various";
                                                    }
                                                    if (title.replace(" ", "").equals(finalReleaseTitle.replace(" ", ""))
                                                            && artistName.replace(" ", "")
                                                            .equals(parsedArtist.replace(" ", ""))) {

                                                        String albumURI = currentResult.getString("uri");
                                                        setSpotifyButton(albumURI);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            })

            {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    finalMStatusCode[0] = response.statusCode;
                    return super.parseNetworkResponse(response);
                }
            };
            queue.add(stringRequest);
        }


    }

    private void setSpotifyButton(final String _albumURI) {
        mSpotifyButton.setVisibility(View.VISIBLE);
        mSpotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(_albumURI));
                startActivity(launcher);
            }
        });
    }
}