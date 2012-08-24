package edu.mit.mobile.android.livingpostcards.data;

import edu.mit.mobile.android.content.column.DBForeignKeyColumn;

public class Authorable {

    public static interface Columns {
        @DBForeignKeyColumn(parent = User.class)
        public static final String AUTHOR = "author";

    }
}
