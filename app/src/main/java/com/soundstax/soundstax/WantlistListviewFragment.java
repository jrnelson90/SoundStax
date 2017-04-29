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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Fragment for Discogs Wantlist Release List
 * Created by jrnel on 2/18/2017.
 */

public class WantlistListviewFragment extends Fragment {
    private static final String TAG = "WantlistListview";
    private RecyclerView mReleaseRecyclerView;
    private Spinner mGenreFilterSpinner;
    private ReleaseAdapter mAdapter;
    private UserWantlistDB mUserWantlistDB;
    private RequestQueue queue;
    private JSONObject mSearchResults;
    private SearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mUserWantlistDB = UserWantlistDB.get(getActivity());
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_release_list, container, false);

        mReleaseRecyclerView = (RecyclerView) view.findViewById(R.id.release_recycler_view);
        mReleaseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        mGenreFilterSpinner = (Spinner) view.findViewById(R.id.release_genre_filter_spinner);
//        mGenreFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (mAdapter != null) {
//                    if (String.valueOf(mGenreFilterSpinner.getSelectedItem()).equals("(All)")) {
//                        List<Release> allReleases = mUserWantlistDB.getReleases();
//                        mAdapter.setReleases(allReleases);
//                    } else {
//                        List<Release> filteredReleases = mUserWantlistDB.getFilteredReleases(
//                                String.valueOf(mGenreFilterSpinner.getSelectedItem()));
//                        mAdapter.setReleases(filteredReleases);
//                    }
//
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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
                Intent i = new Intent(getActivity(), SearchResultsActivity.class);
                Bundle args = new Bundle();
                args.putString("query", query);
                i.putExtras(args);
                startActivity(i);
                return true;
            }
        });
    }

    private void updateUI() {
//        UserWantlistDB releaseBase = UserWantlistDB.get(getActivity());
        List<Release> releases = mUserWantlistDB.getReleases();

        if (mAdapter == null) {
            mAdapter = new ReleaseAdapter(releases);
            mReleaseRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setReleases(releases);
            mAdapter.notifyDataSetChanged();
        }

//        ArrayAdapter<String> genreAdpater = new ArrayAdapter<>(
//                this.getContext(), android.R.layout.simple_spinner_item, mUserWantlistDB.getGenreList());
//        genreAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mGenreFilterSpinner.setAdapter(genreAdpater);
    }

//    void fetchQuery(String _queryString) {
//        //Parse search into a URL friendly encoding.
//        String query_string_encoded = "";
//        try {
//            query_string_encoded = URLEncoder.encode(_queryString, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            System.out.println(e.getMessage());
//        }
//        // Get JSON object for passed release info.
//        String searchString = "https://api.discogs.com/database/search?q=" + query_string_encoded;
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                (Request.Method.GET, searchString, null, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        mSearchResults = response;
//                        Log.i(TAG, "Received Search JSON:");
//
//                        // TODO Call and populate results view
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // Auto-generated method stub
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                Long tsLong = System.currentTimeMillis() / 1000;
//                String ts = tsLong.toString();
//                params.put("Content-Type", "application/x-www-form-urlencoded");
//                params.put("Authorization", "OAuth" +
//                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
//                        ", oauth_nonce=" + ts +
//                        ", oauth_token=" + Preferences.get(Preferences.OAUTH_ACCESS_KEY, "") +
//                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
//                        Preferences.get(Preferences.OAUTH_ACCESS_SECRET, "") +
//                        ", oauth_signature_method=PLAINTEXT" +
//                        ", oauth_timestamp=" + ts);
//                params.put("User-Agent", HttpConst.USER_AGENT);
//                return params;
//            }
//        };
//        // Access the RequestQueue through your singleton class.
//        queue.add(jsObjRequest);
//
//    }

    private class ReleaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTitleTextView;
        private final TextView mArtistTextView;
        private final TextView mYearTextView;
        private final TextView mGenreTextView;
        private final ImageView mThumbImageView;

        private Release mRelease;

        ReleaseHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_release_title_text_view);
            mArtistTextView = (TextView) itemView.findViewById(R.id.list_item_release_artist_text_view);
            mYearTextView = (TextView) itemView.findViewById(R.id.list_item_release_year_text_view);
            mGenreTextView = (TextView) itemView.findViewById(R.id.list_item_release_genre_text_view);
            mThumbImageView = (ImageView) itemView.findViewById(R.id.list_item_release_thumb_image_view);
            setIsRecyclable(false);
        }

        void bindRelease(Release release) {
            mRelease = release;
            mTitleTextView.setText(mRelease.getTitle());
            mArtistTextView.setText(mRelease.getArtist());
            mYearTextView.setText(mRelease.getYear());
            mGenreTextView.setText(mRelease.getGenre());
            mThumbImageView.setImageBitmap(BitmapFactory.decodeFile(mRelease.getThumbDir()));
        }

        @Override
        public void onClick(View v) {
            Intent intent = ReleaseActivity.newIntent(getActivity(), mRelease.getId(), "Wantlist");
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
            if (release.getThumbDir().equals("")) {
                ImageRequest thumbRequest = new ImageRequest(release.getThumbUrl(),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap releaseCoverBitmap) {
                                try {
                                    // path to /data/data/yourapp/app_data/imageDir
                                    ContextWrapper cw = new ContextWrapper(getContext());

                                    String thumbDir = "WantlistCovers";
                                    File directory = cw.getDir(thumbDir, Context.MODE_PRIVATE);
                                    // Create imageDir
//                                    File filePath = new File(directory, "release_cover" +
//                                            holder.getAdapterPosition() + ".jpeg");
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
                                    mUserWantlistDB.updateRelease(release);
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
