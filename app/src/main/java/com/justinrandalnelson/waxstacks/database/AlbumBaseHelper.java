package com.justinrandalnelson.waxstacks.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.justinrandalnelson.waxstacks.Album;
import com.justinrandalnelson.waxstacks.database.AlbumDbSchema.AlbumTable;

/**
 * Created by jrnel on 3/1/2017.
 */

public class AlbumBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "albumBase.db";

    public AlbumBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AlbumTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AlbumTable.Cols.UUID + ", " +
                AlbumTable.Cols.TITLE + ", " +
                AlbumTable.Cols.ARTIST + ", " +
                AlbumTable.Cols.GENRE + ", " +
                AlbumTable.Cols.YEAR +
                ")"
        );

//        populateTestDb(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void populateTestDb(SQLiteDatabase db) {
        final String[] testAlbum =
                {"The Dark Side of the Moon", "Rocket to Russia", "By the Way", "Please Please Me", "Nevermind",
                        "Who's Next", "American Idiot", "Bridge Over Troubled Water", "The Clash", "AM", "Goodbye Yellow Brick Road",
                        "I Love Rock 'n Roll", "Nonagon Infinity", "You Don't Mess Around With Jim", "Peace is the Mission",
                        "Fresh Fruit for Rotting Vegetables", "Fleetwood Mac", "Imagine", "Lunatic", "I Just Can't Stop It"};
        final String[] testArtist =
                {"Pink Floyd", "Ramones", "Red Hot Chili Peppers", "The Beatles", "Nirvana", "The Who", "Green Day",
                        "Simon and Garfunkel", "The Clash", "Arctic Monkeys", "Elton John", "Joan Jett & the Blackhearts",
                        "King Gizzard and the Lizard Wizard", "Jim Croce", "Major Lazer", "Dead Kennedys", "Fleetwood Mac",
                        "John Lennon", "Kongos", "The English Beat"};
        final String[] testGenre =
                {"Classic Rock", "Punk", "Alternative Rock", "Rock & Roll", "Grunge", "Classic Rock",
                "Pop Punk", "Classic Rock", "Punk", "Alternative Rock", "Classic Rock", "Classic Rock",
                "Alternative Rock", "Classic Rock", "Electro", "Punk", "Classic Rock", "Classic Rock",
                "Alternative Rock", "Ska"};
        final String[] testYear =
                {"1973", "1977", "2002", "1963", "1991", "1971", "2004", "1970", "1977", "2013", "1973", "1981",
                        "2016", "1972", "2015", "1980", "1975", "1971", "2012", "1980"};

        for (int i = 0; i < testAlbum.length; i++) {
            Album album = new Album();
            album.setTitle(testAlbum[i]);
            album.setArtist(testArtist[i]);
            album.setGenre(testGenre[i]);
            album.setYear(testYear[i]);
            ContentValues values = new ContentValues();

            values.put(AlbumTable.Cols.UUID, album.getId().toString());
            values.put(AlbumTable.Cols.TITLE, album.getTitle());
            values.put(AlbumTable.Cols.ARTIST, album.getArtist());
            values.put(AlbumTable.Cols.GENRE, album.getGenre());
            values.put(AlbumTable.Cols.YEAR, album.getYear());

            db.insert(AlbumTable.NAME, null, values);
        }
    }
}
