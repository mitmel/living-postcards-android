package edu.mit.mobile.android.livingpostcards.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.QuerystringWrapper;
import edu.mit.mobile.android.locast.data.JsonSyncableItem;
import edu.mit.mobile.android.locast.data.NoPublicPath;
import edu.mit.mobile.android.locast.net.NetworkClient;
import edu.mit.mobile.android.locast.sync.SyncableSimpleContentProvider;

public class CardProvider extends SyncableSimpleContentProvider {

    public static final String AUTHORITY = "edu.mit.mobile.android.livingpostcards";

    public static final int VERSION = 6;

    protected static final String TAG = CardProvider.class.getSimpleName();

    public CardProvider() {
        super(AUTHORITY, VERSION);

        final QuerystringWrapper cards = new QuerystringWrapper(new GenericDBHelper(Card.class));

        final GenericDBHelper users = new GenericDBHelper(User.class);

        // content://authority/card
        // content://authority/card/1
        addDirAndItemUri(cards, Card.PATH);

        addDirAndItemUri(users, User.PATH);

        final ForeignKeyDBHelper cardmedia = new ForeignKeyDBHelper(Card.class, CardMedia.class,
                CardMedia.COL_CARD);

        // content://authority/card/1/media
        // content://authority/card/1/media/1
        addChildDirAndItemUri(cardmedia, Card.PATH, CardMedia.PATH);

    }

    @Override
    public boolean canSync(Uri uri) {
        // TODO Auto-generated method stub
        Log.d(TAG, uri + " can sync");
        return true;
    }

    @Override
    public String getPostPath(Context context, Uri uri) throws NoPublicPath {
        // item post paths are the public path of the dir
        if (getType(uri).startsWith(ProviderUtils.TYPE_ITEM_PREFIX)) {
            uri = ProviderUtils.removeLastPathSegment(uri);
        }
        return getPublicPath(context, uri);
    }

    @Override
    public String getPublicPath(Context context, Uri uri) throws NoPublicPath {
        Log.d(TAG, "getPublicPath " + uri);
        final String type = getType(uri);


        if (Card.CONTENT_URI.equals(uri)) {
            return NetworkClient.getBaseUrlFromManifest(context) + "postcard/";

            // TODO find a way to make this generic. Inspect the SYNC_MAP somehow?
        } else if (CardMedia.TYPE_DIR.equals(type)) {
            return JsonSyncableItem.SyncChildRelation.getPathFromField(context,
                    CardMedia.getCard(uri), Card.COL_MEDIA_URL);
        } else {
            return super.getPublicPath(context, uri);
        }
    }

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}
