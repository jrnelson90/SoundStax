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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Fragment for Discogs Collection Release List
 * Created by jrnel on 2/18/2017.
 */

public class CollectionListviewFragment extends Fragment {

    private static final String TAG = "CollectionListviewFragment";
    private RecyclerView mReleaseRecyclerView;
    private Spinner mFolderFilterSpinner;
    private ReleaseAdapter mAdapter;
    private UserCollectionDB mUserCollectionDB;
    private RequestQueue queue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mUserCollectionDB = UserCollectionDB.get(getActivity());
        queue = VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_release_list, container, false);

        mReleaseRecyclerView = (RecyclerView) view.findViewById(R.id.release_recycler_view);
        mReleaseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mFolderFilterSpinner = (Spinner) view.findViewById(R.id.release_folder_filter_spinner);
        mFolderFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mAdapter != null) {
                    if (String.valueOf(mFolderFilterSpinner.getSelectedItem()).equals("(All)")) {
                        List<Release> allReleases = mUserCollectionDB.getReleases();
                        mAdapter.setReleases(allReleases);
                    } else {
                        List<Release> filteredReleases = mUserCollectionDB.getFilteredReleases(
                                String.valueOf(mFolderFilterSpinner.getSelectedItem()));
                        mAdapter.setReleases(filteredReleases);
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

    private void updateUI() {
        try {
            List<Release> releases = mUserCollectionDB.getReleases();

            if (releases.size() > 0) {
                if (mAdapter == null) {
                    mAdapter = new ReleaseAdapter(releases);
                    mReleaseRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.setReleases(releases);
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                mReleaseRecyclerView.setVisibility(View.GONE);
                RelativeLayout listLayout =
                        (RelativeLayout) getView().findViewById(R.id.list_view_layout);
                TextView errorTextView = new TextView(getContext());
                String errorString = "Collection is Empty";
                errorTextView.setText(errorString);
                listLayout.addView(errorTextView);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> folderAdpater = new ArrayAdapter<>(
                this.getContext(), android.R.layout.simple_spinner_item, mUserCollectionDB.getFolderList());
        folderAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFolderFilterSpinner.setAdapter(folderAdpater);
    }

    private class ReleaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mThumbImageView;
        private final TextView mTitleTextView;
        private final TextView mArtistTextView;
        private final TextView mYearTextView;
        private final TextView mGenreTextView;
        private final TextView mFormatInfo;

        private Release mRelease;

        ReleaseHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mThumbImageView = (ImageView) itemView.findViewById(R.id.list_item_release_thumb_image_view);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_release_title_text_view);
            mArtistTextView = (TextView) itemView.findViewById(R.id.list_item_release_artist_text_view);
            mYearTextView = (TextView) itemView.findViewById(R.id.list_item_release_year_text_view);
            mGenreTextView = (TextView) itemView.findViewById(R.id.list_item_release_folder_text_view);
            mFormatInfo = (TextView) itemView.findViewById(R.id.list_item_release_format_info_view);
            setIsRecyclable(false);
        }

        void bindRelease(Release release) {
            mRelease = release;
            mTitleTextView.setText(mRelease.getTitle());
            mArtistTextView.setText(mRelease.getArtist());
            mYearTextView.setText(mRelease.getYear());
//            mGenreTextView.setText(mRelease.getGenre());
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
            mFormatInfo.append(" (" + formatInfoParsed);
            if (mRelease.getFormatText() != null && mRelease.getFormatText().length() > 0) {
                mFormatInfo.append(" " + mRelease.getFormatText() + ")");
            } else {
                mFormatInfo.append(")");
            }
            mThumbImageView.setImageBitmap(BitmapFactory.decodeFile(mRelease.getThumbDir()));
        }

        @Override
        public void onClick(View v) {
            Intent intent = ReleaseActivity.newIntent(getActivity(), mRelease.getId(), "Collection");
            startActivity(intent);
        }
    }

    private class ReleaseAdapter extends RecyclerView.Adapter<ReleaseHolder> {
        private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
        private static final int VIEW_TYPE_OBJECT_VIEW = 1;
        private List<Release> mReleases;

        ReleaseAdapter(List<Release> releases) {
            mReleases = releases;
        }

        @Override
        public int getItemViewType(int position) {
            if (mReleases.isEmpty()) {
                return VIEW_TYPE_EMPTY_LIST_PLACEHOLDER;
            } else {
                return VIEW_TYPE_OBJECT_VIEW;
            }
        }

        @Override
        public ReleaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_release, parent, false);
            if (viewType == VIEW_TYPE_EMPTY_LIST_PLACEHOLDER) {
                view = layoutInflater.inflate(R.layout.list_item_empty_list, parent, false);
                TextView errorText = (TextView) view.findViewById(R.id.empty_list_text_view);
                String errorString = "Collection is Empty";
                errorText.setText(errorString);
            }
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
                                    ContextWrapper cw = new ContextWrapper(getContext());

                                    String thumbDir = "CollectionCovers";
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
                                    mUserCollectionDB.updateRelease(release);
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

                String thumbDir = "CollectionCovers";
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
                mUserCollectionDB.updateRelease(release);
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
