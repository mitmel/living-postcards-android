package edu.mit.mobile.android.livingpostcards.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import edu.mit.mobile.android.content.DBSortOrder;
import edu.mit.mobile.android.content.ForeignKeyManager;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.IntegerColumn;
import edu.mit.mobile.android.content.column.TextColumn;

@UriPath(Card.PATH)
@DBSortOrder(Card.SORT_DEFAULT)
public class Card implements BaseContentItem, Authorable.Columns {

    public static final String PATH = "card";

    @DBColumn(type = TextColumn.class)
    public static final String NAME = "name";

    @DBColumn(type = TextColumn.class)
    public static final String DESCRIPTION = "description";

    @DBColumn(type = TextColumn.class, unique = true, notnull = true)
    public static final String UUID = "uuid";

    @DBColumn(type = IntegerColumn.class, notnull = true)
    public static final String TIMING = "timing";

    public static final ForeignKeyManager MEDIA = new ForeignKeyManager(CardMedia.class);

    public static Uri createNewCard(ContentResolver cr, String name) {

        final ContentValues cv = new ContentValues();

        cv.put(Card.UUID, java.util.UUID.randomUUID().toString());

        cv.put(Card.NAME, name);

        final Uri card = cr.insert(Card.CONTENT_URI, cv);

        return card;
    }

    public static final String SORT_DEFAULT = CREATION_DATE + " ASC";

    public static final Uri CONTENT_URI = ProviderUtils.toContentUri(CardProvider.AUTHORITY, PATH);
}
