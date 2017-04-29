package com.soundstax.soundstax;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

public class Release implements Serializable {
    private UUID mId;
    private String mTitle;
    private String mYear;
    private String mArtist;
    private String mGenre;
    private String mThumbUrl;
    private String mThumbDir;
    private String mReleaseId;
    private String mInstanceId;
    private String mDateAdded;
    private String mFormatName;
    private String mFormatQty;
    private String mFormatDescriptions;


    private String[] mFormatDescriptionsArray;
    private String mFormatText;

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

    public String getInstanceId() {
        return mInstanceId;
    }

    public void setInstanceId(String instanceId) {
        mInstanceId = instanceId;
    }

    public String getDateAdded() {
        return mDateAdded;
    }

    public void setDateAdded(String dateAdded) {
        mDateAdded = dateAdded;
    }

    public String getFormatName() {
        return mFormatName;
    }

    public void setFormatName(String formatName) {
        mFormatName = formatName;
    }

    public String getFormatQty() {
        return mFormatQty;
    }

    public void setFormatQty(String formatQty) {
        mFormatQty = formatQty;
    }

    public String getFormatDescriptions() {
        return mFormatDescriptions;
    }

    public void setFormatDescriptions(String formatDescriptions) {
        mFormatDescriptions = formatDescriptions;
        String parsedArrayString = formatDescriptions.replaceAll("[\\]\\[\"]", "").replace("\\", "")
                .replace("7", "7\"").replace("10", "10\"").replace("12", "12\"");
        parsedArrayString = parsedArrayString.replace("Limited Edition", "Ltd").replace("Compilation", "Comp")
                .replace("Reissue", "RE").replace("Numbered", "Num").replace("Deluxe Edition", "Dlx")
                .replace("Special Edition", "S/Edition");
        setFormatDescriptionsArray(parsedArrayString.split(","));
    }

    public String[] getFormatDescriptionsArray() {
        return mFormatDescriptionsArray;
    }

    public void setFormatDescriptionsArray(String[] formatDescriptionsArray) {
        mFormatDescriptionsArray = formatDescriptionsArray;
    }

    public String getFormatText() {
        return mFormatText;
    }

    public void setFormatText(String formatText) {
        mFormatText = formatText;
    }
}
