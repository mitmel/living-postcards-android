package edu.mit.mobile.android.livingpostcards.data;

import edu.mit.mobile.android.content.DBSortOrder;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
import edu.mit.mobile.android.content.column.TextColumn;

@UriPath(CardMedia.PATH)
@DBSortOrder(CardMedia.SORT_DEFAULT)
public class CardMedia implements BaseContentItem, Authorable.Columns {

    @DBColumn(type = TextColumn.class, unique = true, notnull = true)
    public static final String UUID = "uuid";

    @DBColumn(type = TextColumn.class)
    public static final String MEDIA_URL = "media_url";

    @DBColumn(type = TextColumn.class)
    public static final String MEDIA_LOCAL_URL = "media_local_url";

    @DBForeignKeyColumn(parent = Card.class)
    public static final String CARD = "card";

    public static final String SORT_DEFAULT = CREATION_DATE + " ASC";

    public static final String PATH = "media";
}
