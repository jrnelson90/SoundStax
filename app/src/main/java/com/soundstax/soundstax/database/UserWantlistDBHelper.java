package com.soundstax.soundstax.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.soundstax.soundstax.database.ReleaseDbSchema.WantlistTable;

/**
 * Created by jrnel on 3/1/2017.
 */

public class UserWantlistDBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "userWantlist.db";

    public UserWantlistDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + WantlistTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                WantlistTable.Cols.UUID + ", " +
                WantlistTable.Cols.TITLE + ", " +
                WantlistTable.Cols.ARTIST + ", " +
                WantlistTable.Cols.GENRE + ", " +
                WantlistTable.Cols.YEAR + ", " +
                WantlistTable.Cols.FORMAT_NAME + ", " +
                WantlistTable.Cols.FORMAT_QTY + ", " +
                WantlistTable.Cols.FORMAT_DESCRIPTIONS + ", " +
                WantlistTable.Cols.FORMAT_TEXT + ", " +
                WantlistTable.Cols.RELEASE_ID + ", " +
                WantlistTable.Cols.DATE_ADDED + ", " +
                WantlistTable.Cols.THUMB_URL + ", " +
                WantlistTable.Cols.THUMB_DIR +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}