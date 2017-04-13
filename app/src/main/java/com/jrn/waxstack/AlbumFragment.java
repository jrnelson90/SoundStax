package com.jrn.waxstack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

public class AlbumFragment extends Fragment {
    private static final String ARG_ALBUM_ID = "album_id";
    private Album mAlbum;
    private EditText mTitleField;

    public static AlbumFragment newInstance(UUID albumID) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALBUM_ID, albumID);

        final AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID albumID = (UUID) getArguments().getSerializable(ARG_ALBUM_ID);
        mAlbum = AlbumBase.get(getActivity()).getAlbum(albumID);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mAlbum.getTitle() != null && mAlbum.getArtist() != null
                && mAlbum.getGenre() != null && mAlbum.getYear() != null) {
            AlbumBase.get(getActivity()).updateAlbum(mAlbum);
        } else {
            AlbumBase.get(getActivity()).deleteAlbum(mAlbum);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);

        mTitleField = (EditText) v.findViewById(R.id.album_title);
        mTitleField.setText(mAlbum.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAlbum.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText artistField = (EditText) v.findViewById(R.id.album_artist);
        artistField.setText(mAlbum.getArtist());
        artistField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAlbum.setArtist(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText genreField = (EditText) v.findViewById(R.id.album_genre);
        genreField.setText(mAlbum.getGenre());
        genreField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAlbum.setGenre(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText yearField = (EditText) v.findViewById(R.id.album_year);
        yearField.setText(mAlbum.getYear());
        yearField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAlbum.setYear(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CheckBox ownedCheckBox = (CheckBox) v.findViewById(R.id.album_owned);
        ownedCheckBox.setChecked(mAlbum.isOwned());
        ownedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAlbum.setOwned(isChecked);
            }
        });

        Button mDeleteAlbumButton = (Button) v.findViewById(R.id.delete_album_button);
        mDeleteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), mTitleField.getText().toString() + " deleted.", Toast.LENGTH_SHORT).show();
                AlbumBase.get(getActivity()).deleteAlbum(mAlbum);
                getActivity().finish();
            }
        });

        return v;
    }
}