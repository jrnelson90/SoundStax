package com.justinrandalnelson.waxstacks.database;

/**
 * Created by jrnel on 3/1/2017.
 */

public class AlbumDbSchema {
    public static final class CollectionTable {
        public static final String NAME = "userCollection";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String ARTIST = "artist";
            public static final String GENRE = "genre";
            public static final String YEAR = "year";
        }
    }

    public static final class WantlistTable {
        public static final String NAME = "userWantlist";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String ARTIST = "artist";
            public static final String GENRE = "genre";
            public static final String YEAR = "year";
        }
    }

}
