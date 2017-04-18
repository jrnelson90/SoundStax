package com.justinrandalnelson.waxstacks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Fragment for Discogs Collection Release List
 * Created by jrnel on 2/18/2017.
 */

public class CollectionListviewFragment extends Fragment {

    private static final String TAG = "CollectionListviewFragment";
    private RecyclerView mReleaseRecyclerView;
    private Spinner mGenreFilterSpinner;
    private ReleaseAdapter mAdapter;
    private JSONObject mArtistResultsJSON = new JSONObject();
    private JSONObject mReleaseReleaseResultsJSON = new JSONObject();
    private JSONObject mUserInfoJSON = new JSONObject();
    private UserCollectionDB mUserCollectionDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mUserCollectionDB = UserCollectionDB.get(getActivity());
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
//                        List<Release> allReleases = mUserCollectionDB.getReleases();
//                        mAdapter.setReleases(allReleases);
//                    } else {
//                        List<Release> filteredReleases = mUserCollectionDB.getFilteredReleases(
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
        inflater.inflate(R.menu.fragment_release_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_release:
                Release release = new Release();
                UserCollectionDB.get(getActivity()).addRelease(release);
                Intent intent = ReleaseActivity.newIntent(getActivity(), release.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
//        UserCollectionDB releaseBase = UserCollectionDB.get(getActivity());
        List<Release> releases = mUserCollectionDB.getReleases();

        if (mAdapter == null) {
            mAdapter = new ReleaseAdapter(releases);
            mReleaseRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setReleases(releases);
            mAdapter.notifyDataSetChanged();
        }

//        ArrayAdapter<String> genreAdpater = new ArrayAdapter<>(
//                this.getContext(), android.R.layout.simple_spinner_item, mUserCollectionDB.getGenreList());
//        genreAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mGenreFilterSpinner.setAdapter(genreAdpater);
    }

    private void updateData() {
        String name = "";
        int index = 0;
        String data = "";
        JSONObject currentPlanet = null;
    }

    private class ReleaseHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

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
        }

        void bindRelease(Release release) {
            mRelease = release;
            mTitleTextView.setText(mRelease.getTitle());
            mArtistTextView.setText(mRelease.getArtist());
            mYearTextView.setText(mRelease.getYear());
            mGenreTextView.setText(mRelease.getGenre());

            try {
                Bitmap thumbBitmap = BitmapFactory.decodeStream(new FileInputStream(mRelease.getThumbDir()));
                mThumbImageView.setImageBitmap(thumbBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = ReleaseActivity.newIntent(getActivity(), mRelease.getId());
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
        public void onBindViewHolder(ReleaseHolder holder, int position) {
            Release release = mReleases.get(position);
            holder.bindRelease(release);
        }

        @Override
        public int getItemCount() {
            return mReleases.size();
        }

        void setReleases(List<Release> releases) {
            mReleases = releases;
        }

    }

    private class FetchArtistJSON extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String artistSearchString = params[0];
            return new JsonFetcher().fetchArtist(artistSearchString);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mArtistResultsJSON = jsonObject;
            updateData();
        }
    }

    private class FetchReleaseJSON extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String releaseReleaseSearchString = params[0];
            return new JsonFetcher().fetchReleaseRelease(releaseReleaseSearchString);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mReleaseReleaseResultsJSON = jsonObject;
            updateData();
        }
    }

}
