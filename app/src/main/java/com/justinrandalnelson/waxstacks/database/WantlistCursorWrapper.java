package com.justinrandalnelson.waxstacks.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.justinrandalnelson.waxstacks.Album;
import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.WantlistTable;

import java.util.UUID;

/**
 * Created by jrnel on 3/1/2017.
 */

public class WantlistCursorWrapper extends CursorWrapper {
    public WantlistCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Album getAlbum() {
        String uuidString = getString(getColumnIndex(WantlistTable.Cols.UUID));
        String title = getString(getColumnIndex(WantlistTable.Cols.TITLE));
        String artist = getString(getColumnIndex(WantlistTable.Cols.ARTIST));
        String genre = getString(getColumnIndex(WantlistTable.Cols.GENRE));
        String year = getString(getColumnIndex(WantlistTable.Cols.YEAR));

        Album album = new Album(UUID.fromString(uuidString));
        album.setTitle(title);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setYear(year);

        return album;
    }
}
