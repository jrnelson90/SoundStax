package com.soundstax.soundstax.database;

/**
 * Created by jrnel on 4/16/2017.
 */

public class ProfileDbSchema {
    public static final class ProfileTable {
        public static final String NAME = "userProfile";

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

