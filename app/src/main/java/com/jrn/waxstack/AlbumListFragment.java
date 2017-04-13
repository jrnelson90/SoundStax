package com.jrn.waxstack;


import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.List;

/**
 * Fragment for Discogs Collection Album List
 * Created by jrnel on 2/18/2017.
 */

public class AlbumListFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";
    private RecyclerView mAlbumRecyclerView;
    private Spinner mGenreFilterSpinner;
    private AlbumAdapter mAdapter;
    private JSONObject mArtistResultsJSON = new JSONObject();
    private JSONObject mAlbumReleaseResultsJSON = new JSONObject();
    private Button mGoogleSignIn;
    private GoogleApiClient mGoogleApiClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
        new FetchArtistJSON().execute("Modern Action");
        new FetchAlbumReleaseJSON().execute("Fresh Fruit for Rotting Vegetables");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);

        mAlbumRecyclerView = (RecyclerView) view.findViewById(R.id.album_recycler_view);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mGoogleSignIn = (Button) view.findViewById(R.id.sign_in_button);
        mGoogleSignIn.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
//                        signIn();
                        break;
                    // ...
                }
            }
        });

//        private void signIn() {
//            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//            startActivityForResult(signInIntent, RC_SIGN_IN);
//        }

        mGenreFilterSpinner = (Spinner) view.findViewById(R.id.album_genre_filter_spinner);
        mGenreFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mAdapter != null) {
                    AlbumBase albumBase = AlbumBase.get(getActivity());
                    if(String.valueOf(mGenreFilterSpinner.getSelectedItem()).equals("(All)")) {
                        List<Album> allAlbums = albumBase.getAlbums();
                        mAdapter.setAlbums(allAlbums);
                    }
                    else {
                        List<Album> filteredAlbums = albumBase.getFilteredAlbums(
                                String.valueOf(mGenreFilterSpinner.getSelectedItem()));
                        mAdapter.setAlbums(filteredAlbums);
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
        super .onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_album_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_album:
                Album album = new Album();
                AlbumBase.get(getActivity()).addAlbum(album);
                Intent intent = AlbumActivity.newIntent(getActivity(), album.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        AlbumBase albumBase = AlbumBase.get(getActivity());
        List<Album> albums = albumBase.getAlbums();

        if(mAdapter == null){
            mAdapter = new AlbumAdapter(albums);
            mAlbumRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setAlbums(albums);
            mAdapter.notifyDataSetChanged();
        }

        ArrayAdapter<String> genreAdpater = new ArrayAdapter<>(
                this.getContext(), android.R.layout.simple_spinner_item, albumBase.getGenreList());
        genreAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenreFilterSpinner.setAdapter(genreAdpater);
    }

    private void updateData() {
        final int THIRD = 3;
        String name = "";
        int index = 0;
        String data = "";
        JSONObject currentPlanet = null;
//        while(index < mJsonArray.length()) {
//            try {
//                currentPlanet = mJsonArray.getJSONObject(index);
//                name = currentPlanet.getString("name");
//            } catch (JSONException e) {
//                Log.e(TAG, "Object parse failed.", e);
//            }
//            index++;
//        }
//
//        if(currentPlanet != null) {
//            try{
//                JSONArray satellites = currentPlanet.getJSONArray("satellites");
//                if(satellites.length() >= THIRD) {
//                    JSONObject thirdSatellite = satellites.getJSONObject(THIRD - 1);
//                    data = "Name: " + thirdSatellite.getString("name") +
//                            ", Diameter(Km): " + thirdSatellite.getString("diameterKm");
//                }
//            } catch (JSONException e) {
//                Log.e(TAG, "", e);
//            }
//
//            if(!data.equals("")) {
//                // Update a item in the view here with data.
//            }
//        }
    }

    private class AlbumHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        private final TextView mTitleTextView;
        private final TextView mArtistTextView;
        private final TextView mGenreTextView;
        private Album mAlbum;

        AlbumHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_album_title_text_view);
            mArtistTextView = (TextView) itemView.findViewById(R.id.list_item_album_artist_text_view);
            mGenreTextView = (TextView) itemView.findViewById(R.id.list_item_album_genre_text_view);
        }

        void bindAlbum(Album album) {
            mAlbum = album;
            mTitleTextView.setText(mAlbum.getTitle());
            mArtistTextView.setText(mAlbum.getArtist());
            mGenreTextView.setText(mAlbum.getGenre());
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
}
