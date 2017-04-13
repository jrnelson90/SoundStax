package com.jrn.waxstack.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.jrn.waxstack.Album;
import com.jrn.waxstack.database.AlbumDbSchema.AlbumTable;

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
        int isOwned = getInt(getColumnIndex(AlbumTable.Cols.OWNED));

        Album album = new Album(UUID.fromString(uuidString));
        album.setTitle(title);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setYear(year);
        album.setOwned(isOwned != 0);

        return album;
    }
}
