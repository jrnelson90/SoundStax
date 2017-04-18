package com.justinrandalnelson.waxstacks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.CollectionTable;

/**
 * Created by jrnel on 3/1/2017.
 */

public class UserCollectionDBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "userCollection.db";

    public UserCollectionDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CollectionTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                CollectionTable.Cols.UUID + ", " +
                CollectionTable.Cols.TITLE + ", " +
                CollectionTable.Cols.ARTIST + ", " +
                CollectionTable.Cols.GENRE + ", " +
                CollectionTable.Cols.YEAR + ", " +
                CollectionTable.Cols.THUMB_URL + ", " +
                CollectionTable.Cols.THUMB_DIR +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}