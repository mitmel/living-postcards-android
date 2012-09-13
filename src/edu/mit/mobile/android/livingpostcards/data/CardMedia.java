package edu.mit.mobile.android.livingpostcards.data;

import android.database.Cursor;
import android.net.Uri;
import edu.mit.mobile.android.content.DBSortOrder;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
import edu.mit.mobile.android.content.column.TextColumn;
import edu.mit.mobile.android.locast.data.Authorable;
import edu.mit.mobile.android.locast.data.ImageContent;

@UriPath(CardMedia.PATH)
@DBSortOrder(CardMedia.SORT_DEFAULT)
public class CardMedia extends ImageContent implements Authorable.Columns {

    public CardMedia(Cursor c) {
        super(c);
    }

    @DBColumn(type = TextColumn.class, unique = true, notnull = true)
    public static final String COL_UUID = "uuid";

    @DBForeignKeyColumn(parent = Card.class)
    public static final String CARD = "card";

    public static final String SORT_DEFAULT = COL_CREATED_DATE + " ASC";

    public static final String PATH = "media";

    public static class ItemSyncMap extends ImageContent.ItemSyncMap {
        /**
         *
         */
        private static final long serialVersionUID = 6879723724633413905L;

        public ItemSyncMap() {
            super();

            putAll(Authorable.SYNC_MAP);
            put(COL_UUID, new SyncFieldMap("uuid", SyncFieldMap.STRING));
        }
    };

    public static final ItemSyncMap SYNC_MAP = new ItemSyncMap();

    @Override
    public Uri getContentUri() {
        // TODO implement
        return null;
    }
}
