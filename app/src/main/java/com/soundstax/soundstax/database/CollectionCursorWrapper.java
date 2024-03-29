package com.soundstax.soundstax.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.soundstax.soundstax.Release;
import com.soundstax.soundstax.database.ReleaseDbSchema.CollectionTable;

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
        String formatName = getString(getColumnIndex(CollectionTable.Cols.FORMAT_NAME));
        String formatQty = getString(getColumnIndex(CollectionTable.Cols.FORMAT_QTY));
        String formatDescriptions = getString(getColumnIndex(CollectionTable.Cols.FORMAT_DESCRIPTIONS));
        String formatText = getString(getColumnIndex(CollectionTable.Cols.FORMAT_TEXT));
        String releaseId = getString(getColumnIndex(CollectionTable.Cols.RELEASE_ID));
        String instanceId = getString(getColumnIndex(CollectionTable.Cols.INSTANCE_ID));
        String folderId = getString(getColumnIndex(CollectionTable.Cols.FOLDER_ID));
        String folderName = getString(getColumnIndex(CollectionTable.Cols.FOLDER_NAME));
        String dateAdded = getString(getColumnIndex(CollectionTable.Cols.DATE_ADDED));
        String thumbUrl = getString(getColumnIndex(CollectionTable.Cols.THUMB_URL));
        String thumbDir = getString(getColumnIndex(CollectionTable.Cols.THUMB_DIR));

        Release release = new Release(UUID.fromString(uuidString));
        release.setTitle(title);
        release.setArtist(artist);
        release.setGenre(genre);
        release.setYear(year);
        release.setFormatName(formatName);
        release.setFormatQty(formatQty);
        release.setFormatDescriptions(formatDescriptions);
        release.setFormatText(formatText);
        release.setReleaseId(releaseId);
        release.setInstanceId(instanceId);
        release.setFolderId(folderId);
        release.setFolderName(folderName);
        release.setDateAdded(dateAdded);
        release.setThumbUrl(thumbUrl);
        release.setThumbDir(thumbDir);

        return release;
    }
}
