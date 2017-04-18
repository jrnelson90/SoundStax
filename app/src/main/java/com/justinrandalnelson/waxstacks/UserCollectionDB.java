package com.justinrandalnelson.waxstacks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.justinrandalnelson.waxstacks.database.CollectionCursorWrapper;
import com.justinrandalnelson.waxstacks.database.ReleaseDbSchema.CollectionTable;
import com.justinrandalnelson.waxstacks.database.UserCollectionDBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

class UserCollectionDB {
    private static UserCollectionDB sUserCollectionDB;
    private final SQLiteDatabase mCollectionDatabase;

    private UserCollectionDB(Context context) {
        Context context1 = context.getApplicationContext();
        mCollectionDatabase = new UserCollectionDBHelper(context1).getWritableDatabase();
    }

    static UserCollectionDB get(Context context) {
        if (sUserCollectionDB == null) {
            sUserCollectionDB = new UserCollectionDB(context);
        }
        return sUserCollectionDB;
    }

    private static ContentValues getContentValues(Release release) {
        ContentValues values = new ContentValues();
        values.put(CollectionTable.Cols.UUID, release.getId().toString());
        values.put(CollectionTable.Cols.TITLE, release.getTitle());
        values.put(CollectionTable.Cols.ARTIST, release.getArtist());
        values.put(CollectionTable.Cols.GENRE, release.getGenre());
        values.put(CollectionTable.Cols.YEAR, release.getYear());
        values.put(CollectionTable.Cols.RELEASE_ID, release.getReleaseId());
        values.put(CollectionTable.Cols.THUMB_URL, release.getThumbUrl());
        values.put(CollectionTable.Cols.THUMB_DIR, release.getThumbDir());

        return values;
    }

    void addRelease(Release release) {
        ContentValues values = getContentValues(release);
        mCollectionDatabase.insert(CollectionTable.NAME, null, values);
    }

    void deleteRelease(Release release) {
        String selection = CollectionTable.Cols.UUID + " = ?";
        String[] selectionArgs = {release.getId().toString()};
        mCollectionDatabase.delete(CollectionTable.NAME, selection, selectionArgs);
    }

    void deleteAllReleases() {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        mCollectionDatabase.delete(CollectionTable.NAME, null, null);
    }

    List<Release> getReleases() {
        List<Release> releases = new ArrayList<>();

        try (CollectionCursorWrapper cursor = queryReleases(null, null)) {
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

        try (CollectionCursorWrapper cursor = queryReleases(null, null)) {
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

        try (CollectionCursorWrapper cursor = queryReleases(null, null)) {
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

        try (CollectionCursorWrapper cursor = queryReleases(
                CollectionTable.Cols.UUID + " = ?",
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
        mCollectionDatabase.update(CollectionTable.NAME, values,
                CollectionTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private CollectionCursorWrapper queryReleases(String whereClause, String[] whereArgs) {
        Cursor cursor = mCollectionDatabase.query(
                CollectionTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null,
                null
        );

        return new CollectionCursorWrapper(cursor);
    }
}
