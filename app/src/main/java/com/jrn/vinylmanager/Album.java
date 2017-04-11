package com.jrn.vinylmanager;

import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

public class Album {
    private UUID mId;
    private String mTitle;
    private String mYear;
    private String mArtist;
    private String mGenre;
    private boolean mOwned;

    public Album() {
        this(UUID.randomUUID());
    }

    public Album(UUID id) {
        mId = id;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre(String genre) {
        mGenre = genre;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public boolean isOwned() {
        return mOwned;
    }

    public void setOwned(boolean owned) {
        mOwned = owned;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
