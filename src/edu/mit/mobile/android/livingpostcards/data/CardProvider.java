package edu.mit.mobile.android.livingpostcards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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

    public static final int VERSION = 7;

    protected static final String TAG = CardProvider.class.getSimpleName();

    public CardProvider() {
        super(AUTHORITY, VERSION);

        final QuerystringWrapper cards = new QuerystringWrapper(new GenericDBHelper(Card.class) {
            @Override
            public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.beginTransaction();
                try {
                    if (oldVersion == 6 && newVersion == 7) {

                        // adding the web url
                        db.execSQL("ALTER TABLE '" + getTable() + "' ADD COLUMN web_url TEXT");

                        final ContentValues cv = new ContentValues();
                        // invalidate all cards so they'll get updated.
                        cv.put("modified", 0);
                        cv.put("server_modified", 0);
                        db.update(getTable(), cv, null, null);
                    } else {
                        super.upgradeTables(db, oldVersion, newVersion);
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        });

        // unused ATM
        // final GenericDBHelper users = new GenericDBHelper(User.class);

        // content://authority/card
        // content://authority/card/1
        addDirAndItemUri(cards, Card.PATH);

        // addDirAndItemUri(users, User.PATH);

        final ForeignKeyDBHelper cardmedia = new ForeignKeyDBHelper(Card.class, CardMedia.class,
                CardMedia.COL_CARD) {
            @Override
            public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                if (oldVersion == 6 && newVersion == 7) {
                    // do nothing for this type
                } else {
                    super.upgradeTables(db, oldVersion, newVersion);
                }
            }
        };

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
