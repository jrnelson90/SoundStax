package com.justinrandalnelson.waxstacks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.CollectionTable;
import com.justinrandalnelson.waxstacks.database.CollectionCursorWrapper;
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

    private static ContentValues getContentValues(Album album) {
        ContentValues values = new ContentValues();
        values.put(CollectionTable.Cols.UUID, album.getId().toString());
        values.put(CollectionTable.Cols.TITLE, album.getTitle());
        values.put(CollectionTable.Cols.ARTIST, album.getArtist());
        values.put(CollectionTable.Cols.GENRE, album.getGenre());
        values.put(CollectionTable.Cols.YEAR, album.getYear());
        values.put(CollectionTable.Cols.THUMB_URL, album.getThumbUrl());
        values.put(CollectionTable.Cols.THUMB_DIR, album.getThumbDir());

        return values;
    }

    void addAlbum(Album album) {
        ContentValues values = getContentValues(album);
        mCollectionDatabase.insert(CollectionTable.NAME, null, values);
    }

    void deleteAlbum(Album album) {
        String selection = CollectionTable.Cols.UUID + " = ?";
        String[] selectionArgs = { album.getId().toString() };
        mCollectionDatabase.delete(CollectionTable.NAME, selection, selectionArgs);
    }

    List<Album> getAlbums(){
        List<Album> albums = new ArrayList<>();

        try (CollectionCursorWrapper cursor = queryAlbums(null, null)) {
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

        try (CollectionCursorWrapper cursor = queryAlbums(null, null)) {
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

        try (CollectionCursorWrapper cursor = queryAlbums(null, null)) {
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

        try (CollectionCursorWrapper cursor = queryAlbums(
                CollectionTable.Cols.UUID + " = ?",
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
        mCollectionDatabase.update(CollectionTable.NAME, values,
                CollectionTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private CollectionCursorWrapper queryAlbums(String whereClause, String[] whereArgs) {
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
