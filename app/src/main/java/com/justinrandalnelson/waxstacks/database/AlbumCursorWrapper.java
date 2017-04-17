package com.justinrandalnelson.waxstacks.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.justinrandalnelson.waxstacks.Album;
import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.AlbumTable;

import java.util.UUID;

/**
 * Created by jrnel on 3/1/2017.
 */

public class AlbumCursorWrapper extends CursorWrapper {
    public AlbumCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Album getAlbum() {
        String uuidString = getString(getColumnIndex(AlbumTable.Cols.UUID));
        String title = getString(getColumnIndex(AlbumTable.Cols.TITLE));
        String artist = getString(getColumnIndex(AlbumTable.Cols.ARTIST));
        String genre = getString(getColumnIndex(AlbumTable.Cols.GENRE));
        String year = getString(getColumnIndex(AlbumTable.Cols.YEAR));

        Album album = new Album(UUID.fromString(uuidString));
        album.setTitle(title);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setYear(year);

        return album;
    }
}
