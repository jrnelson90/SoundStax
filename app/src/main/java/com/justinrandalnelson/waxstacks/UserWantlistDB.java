package com.justinrandalnelson.waxstacks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.WantlistTable;
import com.justinrandalnelson.waxstacks.database.UserWantlistDBHelper;
import com.justinrandalnelson.waxstacks.database.WantlistCursorWrapper;

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

    private static ContentValues getContentValues(Album album) {
        ContentValues values = new ContentValues();
        values.put(WantlistTable.Cols.UUID, album.getId().toString());
        values.put(WantlistTable.Cols.TITLE, album.getTitle());
        values.put(WantlistTable.Cols.ARTIST, album.getArtist());
        values.put(WantlistTable.Cols.GENRE, album.getGenre());
        values.put(WantlistTable.Cols.YEAR, album.getYear());
        values.put(WantlistTable.Cols.THUMB_URL, album.getThumbUrl());
        values.put(WantlistTable.Cols.THUMB_DIR, album.getThumbDir());

        return values;
    }

    void addAlbum(Album album) {
        ContentValues values = getContentValues(album);
        mWantlistDatabase.insert(WantlistTable.NAME, null, values);
    }

    void deleteAlbum(Album album) {
        String selection = WantlistTable.Cols.UUID + " = ?";
        String[] selectionArgs = {album.getId().toString()};
        mWantlistDatabase.delete(WantlistTable.NAME, selection, selectionArgs);
    }

    void deleteAllAlbums() {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        mWantlistDatabase.delete(WantlistTable.NAME, null, null);
    }

    List<Album> getAlbums() {
        List<Album> albums = new ArrayList<>();

        try (WantlistCursorWrapper cursor = queryAlbums(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                albums.add(cursor.getAlbum());
                cursor.moveToNext();
            }
        }

        return albums;
    }

    List<Album> getFilteredAlbums(String filterContraint) {
        List<Album> albums = new ArrayList<>();

        try (WantlistCursorWrapper cursor = queryAlbums(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getAlbum().getGenre().equals(filterContraint))
                    albums.add(cursor.getAlbum());
                cursor.moveToNext();
            }
        }

        return albums;
    }

    ArrayList<String> getGenreList() {
        ArrayList<String> returnedGenres = new ArrayList<>();
        ArrayList<String> foundGenres = new ArrayList<>();

        try (WantlistCursorWrapper cursor = queryAlbums(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String currentGenre = cursor.getAlbum().getGenre();
                if (!foundGenres.contains(currentGenre)) {
                    foundGenres.add(cursor.getAlbum().getGenre());
                }
                cursor.moveToNext();
            }
        }

        Collections.sort(foundGenres);

        returnedGenres.add("(All)");
        returnedGenres.addAll(foundGenres);

        return returnedGenres;
    }

    Album getAlbum(UUID id) {

        try (WantlistCursorWrapper cursor = queryAlbums(
                WantlistTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getAlbum();
        }
    }

    void updateAlbum(Album album) {
        String uuidString = album.getId().toString();
        ContentValues values = getContentValues(album);
        mWantlistDatabase.update(WantlistTable.NAME, values,
                WantlistTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private WantlistCursorWrapper queryAlbums(String whereClause, String[] whereArgs) {
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
