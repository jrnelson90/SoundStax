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
 * Fragment for Discogs Wantlist Album List
 * Created by jrnel on 2/18/2017.
 */

public class WantlistListviewFragment extends Fragment {

    private static final String TAG = "WantlistListviewFragment";
    private RecyclerView mAlbumRecyclerView;
    private Spinner mGenreFilterSpinner;
    private AlbumAdapter mAdapter;
    private JSONObject mArtistResultsJSON = new JSONObject();
    private JSONObject mAlbumReleaseResultsJSON = new JSONObject();
    private JSONObject mUserInfoJSON = new JSONObject();
    private UserWantlistDB mUserWantlistDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mUserWantlistDB = UserWantlistDB.get(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);

        mAlbumRecyclerView = (RecyclerView) view.findViewById(R.id.album_recycler_view);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        mGenreFilterSpinner = (Spinner) view.findViewById(R.id.album_genre_filter_spinner);
//        mGenreFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (mAdapter != null) {
//                    if (String.valueOf(mGenreFilterSpinner.getSelectedItem()).equals("(All)")) {
//                        List<Album> allAlbums = mUserWantlistDB.getAlbums();
//                        mAdapter.setAlbums(allAlbums);
//                    } else {
//                        List<Album> filteredAlbums = mUserWantlistDB.getFilteredAlbums(
//                                String.valueOf(mGenreFilterSpinner.getSelectedItem()));
//                        mAdapter.setAlbums(filteredAlbums);
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
        inflater.inflate(R.menu.fragment_album_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_album:
                Album album = new Album();
                UserWantlistDB.get(getActivity()).addAlbum(album);
                Intent intent = AlbumActivity.newIntent(getActivity(), album.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
//        UserWantlistDB albumBase = UserWantlistDB.get(getActivity());
        List<Album> albums = mUserWantlistDB.getAlbums();

        if (mAdapter == null) {
            mAdapter = new AlbumAdapter(albums);
            mAlbumRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setAlbums(albums);
            mAdapter.notifyDataSetChanged();
        }

//        ArrayAdapter<String> genreAdpater = new ArrayAdapter<>(
//                this.getContext(), android.R.layout.simple_spinner_item, mUserWantlistDB.getGenreList());
//        genreAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mGenreFilterSpinner.setAdapter(genreAdpater);
    }

    private void updateData() {
        String name = "";
        int index = 0;
        String data = "";
        JSONObject currentPlanet = null;
    }

    private class AlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTitleTextView;
        private final TextView mArtistTextView;
        private final TextView mYearTextView;
        private final TextView mGenreTextView;
        private final ImageView mThumbImageView;

        private Album mAlbum;

        AlbumHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_album_title_text_view);
            mArtistTextView = (TextView) itemView.findViewById(R.id.list_item_album_artist_text_view);
            mYearTextView = (TextView) itemView.findViewById(R.id.list_item_album_year_text_view);
            mGenreTextView = (TextView) itemView.findViewById(R.id.list_item_album_genre_text_view);
            mThumbImageView = (ImageView) itemView.findViewById(R.id.list_item_album_thumb_image_view);
        }

        void bindAlbum(Album album) {
            mAlbum = album;
            mTitleTextView.setText(mAlbum.getTitle());
            mArtistTextView.setText(mAlbum.getArtist());
            mYearTextView.setText(mAlbum.getYear());
            mGenreTextView.setText(mAlbum.getGenre());

            try {
                Bitmap thumbBitmap = BitmapFactory.decodeStream(new FileInputStream(mAlbum.getThumbDir()));
                mThumbImageView.setImageBitmap(thumbBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = AlbumActivity.newIntent(getActivity(), mAlbum.getId());
            startActivity(intent);
        }
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumHolder> {
        private List<Album> mAlbums;

        AlbumAdapter(List<Album> albums) {
            mAlbums = albums;
        }

        @Override
        public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_album, parent, false);
            return new AlbumHolder(view);
        }

        @Override
        public void onBindViewHolder(AlbumHolder holder, int position) {
            Album album = mAlbums.get(position);
            holder.bindAlbum(album);
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }

        void setAlbums(List<Album> albums) {
            mAlbums = albums;
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

    private class FetchAlbumReleaseJSON extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String albumReleaseSearchString = params[0];
            return new JsonFetcher().fetchAlbumRelease(albumReleaseSearchString);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mAlbumReleaseResultsJSON = jsonObject;
            updateData();
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
        }
    }


}
