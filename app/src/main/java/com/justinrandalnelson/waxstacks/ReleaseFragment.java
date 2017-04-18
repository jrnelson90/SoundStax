package com.justinrandalnelson.waxstacks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.UUID;

/**
 * Release Fragment View - Shows details about a particular music item release
 * Created by jrnel on 2/18/2017.
 */

public class ReleaseFragment extends Fragment {
    private static final String ARG_RELEASE_ID = "release_id";
    private Release mRelease;
    private TextView mTitleField;
    private ImageView mReleaseCoverView;
    private JSONObject mReleaseJSON;
    private RequestQueue queue;

    public static ReleaseFragment newInstance(UUID releaseID) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RELEASE_ID, releaseID);
        final ReleaseFragment fragment = new ReleaseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID releaseID = (UUID) getArguments().getSerializable(ARG_RELEASE_ID);
        mRelease = UserCollectionDB.get(getActivity()).getRelease(releaseID);
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        String releaseURL = "https://api.discogs.com/releases/" + mRelease.getReleaseId() + "&token=" +
                Preferences.get(Preferences.OAUTH_ACCESS_KEY, "");
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
        });
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
        genreField.setText(mRelease.getGenre());

        TextView yearField = (TextView) v.findViewById(R.id.release_year);
        yearField.setText(mRelease.getYear());


//        Button mDeleteReleaseButton = (Button) v.findViewById(R.id.delete_release_button);
//        mDeleteReleaseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), mTitleField.getText().toString() + " deleted.", Toast.LENGTH_SHORT).show();
//                UserCollectionDB.get(getActivity()).deleteRelease(mRelease);
//                getActivity().finish();
//            }
//        });

        return v;
    }
}