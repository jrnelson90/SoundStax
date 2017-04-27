package com.justinrandalnelson.waxstacks;

import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

public class Release {
    private UUID mId;
    private String mTitle;
    private String mYear;
    private String mArtist;
    private String mGenre;
    private String mThumbUrl;
    private String mThumbDir;
    private String mReleaseId;

    public Release() {
        this(UUID.randomUUID());
    }

    public Release(UUID id) {
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

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        mThumbUrl = thumbUrl;
    }

    public String getThumbDir() {
        return mThumbDir;
    }

    public void setThumbDir(String thumbDir) {
        mThumbDir = thumbDir;
    }

    public String getReleaseId() {
        return mReleaseId;
    }

    public void setReleaseId(String releaseId) {
        mReleaseId = releaseId;
    }
}
