package com.soundstax.soundstax;

import java.util.ArrayList;

/**
 * Created by jrnel on 4/29/2017.
 */

class UserFolders {
    static ArrayList<Folder> sFolderArrayList = new ArrayList<>();

    static class Folder {
        protected String count;
        protected String id;
        protected String name;

        public String getCount() {
            return count;
        }

        public void setCount(String _count) {
            count = _count;
        }

        public String getId() {
            return id;
        }

        public void setId(String _id) {
            id = _id;
        }

        public String getName() {
            return name;
        }

        public void setName(String _name) {
            name = _name;
        }
    }
}
