package edu.mit.mobile.android.livingpostcards.data;

import android.net.Uri;
import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.ForeignKeyManager;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.TextColumn;

@UriPath(User.PATH)
public class User implements ContentItem {

    @DBColumn(type = TextColumn.class)
    public static final String DISPLAY_NAME = "display_name";

    @DBColumn(type = TextColumn.class)
    public static final String PUBLIC_URL = "url";

    public static final ForeignKeyManager CARDS = new ForeignKeyManager(Card.class);

    public static final String PATH = "user";

    public static final Uri CONTENT_URI = ProviderUtils.toContentUri(CardProvider.AUTHORITY, PATH);
}
