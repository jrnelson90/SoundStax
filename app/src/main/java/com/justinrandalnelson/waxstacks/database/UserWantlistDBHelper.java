package com.justinrandalnelson.waxstacks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.WantlistTable;

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
                WantlistTable.Cols.YEAR +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}