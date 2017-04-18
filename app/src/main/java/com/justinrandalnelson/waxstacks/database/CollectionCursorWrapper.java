package com.justinrandalnelson.waxstacks.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.justinrandalnelson.waxstacks.Album;
import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.CollectionTable;

import java.util.UUID;

/**
 * Created by jrnel on 3/1/2017.
 */

public class CollectionCursorWrapper extends CursorWrapper {
    public CollectionCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Album getAlbum() {
        String uuidString = getString(getColumnIndex(CollectionTable.Cols.UUID));
        String title = getString(getColumnIndex(CollectionTable.Cols.TITLE));
        String artist = getString(getColumnIndex(CollectionTable.Cols.ARTIST));
        String genre = getString(getColumnIndex(CollectionTable.Cols.GENRE));
        String year = getString(getColumnIndex(CollectionTable.Cols.YEAR));
        String thumbUrl = getString(getColumnIndex(CollectionTable.Cols.THUMB_URL));
        String thumbDir = getString(getColumnIndex(CollectionTable.Cols.THUMB_DIR));

        Album album = new Album(UUID.fromString(uuidString));
        album.setTitle(title);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setYear(year);
        album.setThumbUrl(thumbUrl);
        album.setThumbDir(thumbDir);

        return album;
    }
}
