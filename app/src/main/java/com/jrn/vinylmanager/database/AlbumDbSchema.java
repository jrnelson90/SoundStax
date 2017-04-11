package com.jrn.vinylmanager.database;

/**
 * Created by jrnel on 3/1/2017.
 */

public class AlbumDbSchema {
    public static final class AlbumTable {
        public static final String NAME = "albums";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String ARTIST = "artist";
            public static final String GENRE = "genre";
            public static final String YEAR = "year";
            public static final String OWNED = "owned";
        }
    }

}
