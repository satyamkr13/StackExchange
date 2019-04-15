package com.instinotices.satyam.stackexchange.CustomDataTypes;

import java.util.ArrayList;

public class TagItems {
    ArrayList<Tag> items;

    public ArrayList<Tag> getTags() {
        return items;
    }

    public class Tag {
        private String name;

        public String getName() {
            return name;
        }
    }
}
