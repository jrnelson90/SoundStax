package com.justinrandalnelson.waxstacks.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.justinrandalnelson.waxstacks.Release;
import com.justinrandalnelson.waxstacks.database.ReleaseDbSchema.CollectionTable;

import java.util.UUID;

/**
 * Created by jrnel on 3/1/2017.
 */

public class CollectionCursorWrapper extends CursorWrapper {
    public CollectionCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Release getRelease() {
        String uuidString = getString(getColumnIndex(CollectionTable.Cols.UUID));
        String title = getString(getColumnIndex(CollectionTable.Cols.TITLE));
        String artist = getString(getColumnIndex(CollectionTable.Cols.ARTIST));
        String genre = getString(getColumnIndex(CollectionTable.Cols.GENRE));
        String year = getString(getColumnIndex(CollectionTable.Cols.YEAR));
        String releaseId = getString(getColumnIndex(CollectionTable.Cols.RELEASE_ID));
        String thumbUrl = getString(getColumnIndex(CollectionTable.Cols.THUMB_URL));
        String thumbDir = getString(getColumnIndex(CollectionTable.Cols.THUMB_DIR));

        Release release = new Release(UUID.fromString(uuidString));
        release.setTitle(title);
        release.setArtist(artist);
        release.setGenre(genre);
        release.setYear(year);
        release.setReleaseId(releaseId);
        release.setThumbUrl(thumbUrl);
        release.setThumbDir(thumbDir);

        return release;
    }
}
