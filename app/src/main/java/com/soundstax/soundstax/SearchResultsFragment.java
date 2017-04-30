package com.soundstax.soundstax;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying search results
 * Created by jrnel on 4/26/2017.
 */

public class SearchResultsFragment extends Fragment {
    private static final String TAG = "SearchResults";
    private static final String QUERY_ARG = "query";
    private RecyclerView mResultsRecyclerView;
    private JSONObject mSearchResults = null;
    private SearchView mSearchView;
    private RequestQueue queue;
    private ReleaseAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        queue = VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        Bundle args = getActivity().getIntent().getExtras();
        String queryString = args.getString(QUERY_ARG);
        fetchQuery(queryString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_release_list, container, false);

        mResultsRecyclerView = (RecyclerView) view.findViewById(R.id.release_recycler_view);
        mResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        TextView filterLabel = (TextView) view.findViewById(R.id.release_folder_filter_textview);
        filterLabel.setVisibility(View.GONE);
        Spinner filterSpinner = (Spinner) view.findViewById(R.id.release_folder_filter_spinner);
        filterSpinner.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_toolbar_layout, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // your text view here
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchQuery(query);
                return true;
            }
        });
    }

    private void updateUI() {
        List<Release> releases;
        try {
            releases = searchJsonToReleaseList(mSearchResults.getJSONArray("results"));
            if (mAdapter == null) {
                mAdapter = new ReleaseAdapter(releases);
                mResultsRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setReleases(releases);
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void fetchQuery(String _queryString) {
        //Parse search into a URL friendly encoding.
        String query_string_encoded = "";
        try {
            query_string_encoded = URLEncoder.encode(_queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
        // Get JSON object for passed release info.
        String searchString = "https://api.discogs.com/database/search?q=" +
                query_string_encoded + "&per_page=100";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, searchString, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSearchResults = response;
                        Log.i(TAG, "Received Search JSON:");
                        updateUI();
                        // TODO Call and populate results view
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Auto-generated method stub
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

    private List<Release> searchJsonToReleaseList(JSONArray _searchResults) throws JSONException {
        List<Release> returnList = new ArrayList<>();
        for (int i = 0; i < _searchResults.length(); i++) {
            JSONObject currentRelease = (JSONObject) _searchResults.get(i);
            if (currentRelease != null && currentRelease.getString("type").equals("release")) {
                String releaseTitle = currentRelease.getString("title").split("-")[1];
                String releaseYear;
                if (currentRelease.has("year")) {
                    releaseYear = currentRelease.getString("year");
                } else {
                    releaseYear = "Year Unknown";
                }
                String releaseArtist = currentRelease.getString("title").split("-")[0];
                String releaseId = currentRelease.getString("id");
                JSONArray formatInfo = currentRelease.getJSONArray("format");
                String formatName = "";
                String formatDescriptions = "";
                for (int j = 0; j < formatInfo.length(); j++) {
                    if (j == 0) {
                        formatName = formatInfo.getString(j);
                    } else {
                        formatDescriptions += formatInfo.getString(j);

                        if (formatInfo.length() > 2 && j < formatInfo.length() - 1) {
                            formatDescriptions += ", ";
                        }
                    }
                }

                Release release = new Release();
                release.setArtist(releaseArtist);
                release.setYear(releaseYear);
                release.setTitle(releaseTitle);
                release.setReleaseId(releaseId);
                release.setFormatName(formatName);
                release.setFormatDescriptions(formatDescriptions);
                release.setThumbUrl(currentRelease.getString("thumb"));
                release.setThumbDir("");
                returnList.add(release);
            }
        }
        return returnList;
    }

    private class ReleaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTitleTextView;
        private final TextView mArtistTextView;
        private final TextView mYearTextView;
        private final TextView mGenreTextView;
        private final ImageView mThumbImageView;
        private final TextView mFormatInfo;

        private Release mRelease;

        ReleaseHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_release_title_text_view);
            mArtistTextView = (TextView) itemView.findViewById(R.id.list_item_release_artist_text_view);
            mYearTextView = (TextView) itemView.findViewById(R.id.list_item_release_year_text_view);
            mGenreTextView = (TextView) itemView.findViewById(R.id.list_item_release_folder_text_view);
            mThumbImageView = (ImageView) itemView.findViewById(R.id.list_item_release_thumb_image_view);
            mFormatInfo = (TextView) itemView.findViewById(R.id.list_item_release_format_info_view);
            setIsRecyclable(false);
        }

        void bindRelease(Release release) {
            mRelease = release;
            mTitleTextView.setText(mRelease.getTitle());
            mArtistTextView.setText(mRelease.getArtist());
            mYearTextView.setText(mRelease.getYear());
            mGenreTextView.setVisibility(View.GONE);
            mFormatInfo.setText(mRelease.getFormatName());
            String formatInfoParsed = "";
            for (int i = 0; i < mRelease.getFormatDescriptionsArray().length; i++) {
                formatInfoParsed += mRelease.getFormatDescriptionsArray()[i];
                if (mRelease.getFormatDescriptionsArray().length >= 2 &&
                        i != mRelease.getFormatDescriptionsArray().length - 1) {
                    formatInfoParsed += " ";
                }
            }
            mFormatInfo.append(" (" + formatInfoParsed + ")");
            mThumbImageView.setImageBitmap(BitmapFactory.decodeFile(mRelease.getThumbDir()));
        }

        @Override
        public void onClick(View v) {
            Intent intent = ReleaseActivity.newIntent(getActivity(), mRelease.getId(), "Search");
            Bundle args = new Bundle();
            args.putSerializable("release", mRelease);
            intent.putExtras(args);
            startActivity(intent);
        }
    }

    private class ReleaseAdapter extends RecyclerView.Adapter<ReleaseHolder> {
        private List<Release> mReleases;

        ReleaseAdapter(List<Release> releases) {
            mReleases = releases;
        }

        @Override
        public ReleaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_release, parent, false);
            return new ReleaseHolder(view);
        }

        @Override
        public void onBindViewHolder(final ReleaseHolder holder, int position) {
            final Release release = mReleases.get(position);
            if (!release.getThumbUrl().equals("")) {
                if (release.getThumbDir().equals("") && !release.getThumbUrl().equals("local")) {
                    ImageRequest thumbRequest = new ImageRequest(release.getThumbUrl(),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap releaseCoverBitmap) {
                                try {
                                    // path to /data/data/yourapp/app_data/imageDir
                                    ContextWrapper cw = new ContextWrapper(getActivity());

                                    String thumbDir = "SearchCovers";
                                    File directory = cw.getDir(thumbDir, Context.MODE_PRIVATE);
                                    // Create imageDir
                                    File filePath = new File(directory, "release_" +
                                            release.getReleaseId() + "_cover.jpeg");

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

                                    release.setThumbDir(filePath.getAbsolutePath());
                                    holder.bindRelease(release);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 200, 200, ImageView.ScaleType.FIT_CENTER, null, null);
                    // Add the request to the RequestQueue.
                    queue.add(thumbRequest);
                } else {
                    holder.bindRelease(release);
                }
            } else {
                release.setThumbUrl("local");
                Bitmap blankAlbumBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.album_blank);
                // path to /data/data/yourapp/app_data/imageDir
                ContextWrapper cw = new ContextWrapper(getContext());

                String thumbDir = "SearchCovers";
                File directory = cw.getDir(thumbDir, Context.MODE_PRIVATE);

                // Create imageDir
                File filePath = new File(directory, "release_" +
                        release.getReleaseId() + "_cover.jpeg");

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(filePath);
                    blankAlbumBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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

                release.setThumbDir(filePath.getAbsolutePath());
                holder.bindRelease(release);
            }
        }

        @Override
        public int getItemCount() {
            return mReleases.size();
        }

        void setReleases(List<Release> releases) {
            mReleases = releases;
        }

    }
}
