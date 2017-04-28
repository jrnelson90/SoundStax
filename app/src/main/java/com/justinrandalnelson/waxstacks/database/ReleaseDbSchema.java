package com.justinrandalnelson.waxstacks.database;

/**
 * Created by jrnel on 3/1/2017.
 */

public class ReleaseDbSchema {
    public static final class CollectionTable {
        public static final String NAME = "userCollection";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String ARTIST = "artist";
            public static final String GENRE = "genre";
            public static final String YEAR = "year";
            public static final String RELEASE_ID = "releaseId";
            public static final String INSTANCE_ID = "instanceId";
            public static final String THUMB_URL = "thumbUrl";
            public static final String THUMB_DIR = "thumbDir";
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
            public static final String RELEASE_ID = "releaseId";
            public static final String THUMB_URL = "thumbUrl";
            public static final String THUMB_DIR = "thumbDir";
        }
    }

}
