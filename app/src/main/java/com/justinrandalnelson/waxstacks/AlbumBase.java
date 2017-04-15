package com.justinrandalnelson.waxstacks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.justinrandalnelson.waxstacks.database.AlbumBaseHelper;
import com.justinrandalnelson.waxstacks.database.AlbumCursorWrapper;
import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.AlbumTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by jrnel on 2/18/2017.
 */

class AlbumBase {
    private static AlbumBase sAlbumBase;

    private final SQLiteDatabase mDatabase;


    private AlbumBase(Context context) {
        Context context1 = context.getApplicationContext();
        mDatabase = new AlbumBaseHelper(context1).getWritableDatabase();
    }

    static AlbumBase get(Context context) {
        if (sAlbumBase == null) {
            sAlbumBase = new AlbumBase(context);
        }
        return sAlbumBase;
    }

    private static ContentValues getContentValues(Album album) {
        ContentValues values = new ContentValues();
        values.put(AlbumTable.Cols.UUID, album.getId().toString());
        values.put(AlbumTable.Cols.TITLE, album.getTitle());
        values.put(AlbumTable.Cols.ARTIST, album.getArtist());
        values.put(AlbumTable.Cols.GENRE, album.getGenre());
        values.put(AlbumTable.Cols.YEAR, album.getYear());
        values.put(AlbumTable.Cols.OWNED, album.isOwned() ? 1 : 0);

        return values;
    }

    void addAlbum(Album album) {
        ContentValues values = getContentValues(album);
        mDatabase.insert(AlbumTable.NAME, null, values);
    }

    void deleteAlbum(Album album) {
        String selection = AlbumTable.Cols.UUID + " = ?";
        String[] selectionArgs = { album.getId().toString() };
        mDatabase.delete(AlbumTable.NAME, selection, selectionArgs);
    }

    List<Album> getAlbums(){
        List<Album> albums = new ArrayList<>();

        try (AlbumCursorWrapper cursor = queryAlbums(null, null)) {
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

        try (AlbumCursorWrapper cursor = queryAlbums(null, null)) {
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

        try (AlbumCursorWrapper cursor = queryAlbums(null, null)) {
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

        try (AlbumCursorWrapper cursor = queryAlbums(
                AlbumTable.Cols.UUID + " = ?",
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
        mDatabase.update(AlbumTable.NAME, values,
                AlbumTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private AlbumCursorWrapper queryAlbums(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                AlbumTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null,
                null
        );

        return new AlbumCursorWrapper(cursor);
    }
}
