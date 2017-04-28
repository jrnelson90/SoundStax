package com.soundstax.soundstax.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.soundstax.soundstax.Release;
import com.soundstax.soundstax.database.ReleaseDbSchema.WantlistTable;

import java.util.UUID;

/**
 * Created by jrnel on 3/1/2017.
 */

public class WantlistCursorWrapper extends CursorWrapper {
    public WantlistCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Release getRelease() {
        String uuidString = getString(getColumnIndex(WantlistTable.Cols.UUID));
        String title = getString(getColumnIndex(WantlistTable.Cols.TITLE));
        String artist = getString(getColumnIndex(WantlistTable.Cols.ARTIST));
        String genre = getString(getColumnIndex(WantlistTable.Cols.GENRE));
        String year = getString(getColumnIndex(WantlistTable.Cols.YEAR));
        String releaseId = getString(getColumnIndex(WantlistTable.Cols.RELEASE_ID));
        String thumbUrl = getString(getColumnIndex(WantlistTable.Cols.THUMB_URL));
        String thumbDir = getString(getColumnIndex(WantlistTable.Cols.THUMB_DIR));

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
