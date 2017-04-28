package com.soundstax.soundstax;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.soundstax.soundstax.database.ReleaseDbSchema.WantlistTable;
import com.soundstax.soundstax.database.UserWantlistDBHelper;
import com.soundstax.soundstax.database.WantlistCursorWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

class UserWantlistDB {
    private static UserWantlistDB sUserWantlistDB;
    private final SQLiteDatabase mWantlistDatabase;

    private UserWantlistDB(Context context) {
        Context context1 = context.getApplicationContext();
        mWantlistDatabase = new UserWantlistDBHelper(context1).getWritableDatabase();
    }

    static UserWantlistDB get(Context context) {
        if (sUserWantlistDB == null) {
            sUserWantlistDB = new UserWantlistDB(context);
        }
        return sUserWantlistDB;
    }

    private static ContentValues getContentValues(Release release) {
        ContentValues values = new ContentValues();
        values.put(WantlistTable.Cols.UUID, release.getId().toString());
        values.put(WantlistTable.Cols.TITLE, release.getTitle());
        values.put(WantlistTable.Cols.ARTIST, release.getArtist());
        values.put(WantlistTable.Cols.GENRE, release.getGenre());
        values.put(WantlistTable.Cols.YEAR, release.getYear());
        values.put(WantlistTable.Cols.RELEASE_ID, release.getReleaseId());
        values.put(WantlistTable.Cols.THUMB_URL, release.getThumbUrl());
        values.put(WantlistTable.Cols.THUMB_DIR, release.getThumbDir());

        return values;
    }

    void addRelease(Release release) {
        ContentValues values = getContentValues(release);
        mWantlistDatabase.insert(WantlistTable.NAME, null, values);
    }

    void deleteRelease(Release release) {
        String selection = WantlistTable.Cols.UUID + " = ?";
        String[] selectionArgs = {release.getId().toString()};
        mWantlistDatabase.delete(WantlistTable.NAME, selection, selectionArgs);
    }

    void deleteAllReleases() {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        mWantlistDatabase.delete(WantlistTable.NAME, null, null);
    }

    List<Release> getReleases() {
        List<Release> releases = new ArrayList<>();

        try (WantlistCursorWrapper cursor = queryReleases(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                releases.add(cursor.getRelease());
                cursor.moveToNext();
            }
        }

        return releases;
    }

    List<Release> getFilteredReleases(String filterContraint) {
        List<Release> releases = new ArrayList<>();

        try (WantlistCursorWrapper cursor = queryReleases(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getRelease().getGenre().equals(filterContraint))
                    releases.add(cursor.getRelease());
                cursor.moveToNext();
            }
        }

        return releases;
    }

    ArrayList<String> getGenreList() {
        ArrayList<String> returnedGenres = new ArrayList<>();
        ArrayList<String> foundGenres = new ArrayList<>();

        try (WantlistCursorWrapper cursor = queryReleases(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String currentGenre = cursor.getRelease().getGenre();
                if (!foundGenres.contains(currentGenre)) {
                    foundGenres.add(cursor.getRelease().getGenre());
                }
                cursor.moveToNext();
            }
        }

        Collections.sort(foundGenres);

        returnedGenres.add("(All)");
        returnedGenres.addAll(foundGenres);

        return returnedGenres;
    }

    Release getRelease(UUID id) {

        try (WantlistCursorWrapper cursor = queryReleases(
                WantlistTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getRelease();
        }
    }

    void updateRelease(Release release) {
        String uuidString = release.getId().toString();
        ContentValues values = getContentValues(release);
        mWantlistDatabase.update(WantlistTable.NAME, values,
                WantlistTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private WantlistCursorWrapper queryReleases(String whereClause, String[] whereArgs) {
        Cursor cursor = mWantlistDatabase.query(
                WantlistTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null,
                null
        );

        return new WantlistCursorWrapper(cursor);
    }
}
