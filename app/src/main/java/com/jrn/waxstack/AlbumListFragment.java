package com.jrn.waxstack;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
    private String[] mRequestToken;
    private String[] mAccessToken;
    private Button mSignInButton;
    private WebView mAuthWebView;
    private Dialog mAuthDialog;
    private ProgressBar mProgressBar;
    private String[] mOauthKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super .onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);

        mAlbumRecyclerView = (RecyclerView) view.findViewById(R.id.album_recycler_view);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mSignInButton = (Button) view.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new FetchArtistJSON().execute("Modern Action");
//                new FetchAlbumReleaseJSON().execute("Fresh Fruit for Rotting Vegetables");
                new FetchRequestToken().execute();
            }
        });

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
//                String[] satellites = currentPlanet.getString[]("satellites");
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

    private void openAuthDialog() {
        mAuthDialog = new Dialog(getContext());
        mAuthDialog.setContentView(R.layout.auth_dialog);
        mProgressBar = (ProgressBar) mAuthDialog.findViewById(R.id.auth_webview_progress_bar);
        mProgressBar.setMax(100);
        mAuthWebView = (WebView) mAuthDialog.findViewById(R.id.webv);
        mAuthWebView.getSettings().setJavaScriptEnabled(true);

        mAuthWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });

        mAuthWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("callback")) {
                    Uri OauthResult = Uri.parse(url);
                    mOauthKey = OauthResult.toString().split("&");
                    mOauthKey[0] = mOauthKey[0].split("//")[1].split("=")[1];
                    mOauthKey[1] = mOauthKey[1].split("=")[1];
                    mAuthDialog.cancel();
                    Log.i("Oauth Success", "Key: " + mOauthKey[0]);
                    Log.i("Oauth Success", "Verifier: " + mOauthKey[1]);

                    new FetchOauthAccessToken().execute(new String[]{mRequestToken[0], mOauthKey[1]});
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        });

        String authUrl;
        if (mRequestToken != null) {
            authUrl = HttpConst.AUTHORIZATION_WEBSITE_URL + "?" + mRequestToken[1];
            Log.i("Auth URL", authUrl);
            mAuthWebView.loadUrl(authUrl);
            mAuthDialog.show();
            mAuthDialog.setCancelable(true);
        } else {
            Log.i("Auth Dialog", "No oauth request token values populated");
        }
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

    private class FetchRequestToken extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            return new JsonFetcher().fetchRequestToken();
        }

        @Override
        protected void onPostExecute(String[] tokenArray) {
            mRequestToken = tokenArray;
            openAuthDialog();
        }
    }

    private class FetchOauthAccessToken extends AsyncTask<String[], Void, String[]> {
        @Override
        protected String[] doInBackground(String[]... params) {
            String[] _passedOauth = params[0];
            return new JsonFetcher().fetchOauthAccessToken(_passedOauth);
        }

        @Override
        protected void onPostExecute(String[] tokenArray) {
            mAccessToken = tokenArray;
        }
    }
}
