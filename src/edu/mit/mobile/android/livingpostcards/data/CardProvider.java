package edu.mit.mobile.android.livingpostcards.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.locast.data.NoPublicPath;
import edu.mit.mobile.android.locast.net.NetworkClient;
import edu.mit.mobile.android.locast.sync.SyncableSimpleContentProvider;

public class CardProvider extends SyncableSimpleContentProvider {

    public static final String AUTHORITY = "edu.mit.mobile.android.livingpostcards";

    public static final int VERSION = 4;

    protected static final String TAG = CardProvider.class.getSimpleName();

    public CardProvider() {
        super(AUTHORITY, VERSION);

        final GenericDBHelper cards = new GenericDBHelper(Card.class) {
            @Override
            public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                if (newVersion != VERSION) {
                    Log.e(TAG, "Don't know how to upgrade to version " + newVersion
                            + ". App DB version is " + VERSION);
                    throw new IllegalStateException("Error upgrading database");
                }
                final String table = getTable();

                if (oldVersion < 2) {

                    db.execSQL("ALTER TABLE '" + table
                            + "' ADD COLUMN timing INTEGER NOT NULL DEFAULT " + Card.DEFAULT_TIMING);
                }

                if (oldVersion < 3) {
                    // db.beginTransaction();
                    // String tmp_table = "tmp_" + table;
                    // db.execSQL("ALTER TABLE '" + table + "' RENAME TO '" + tmp_table + "'");
                    //
                    // createTables(db);
                    //
                    // db.execSQL("INSERT INTO '" + table + "' (name, description, uuid, timing" )
                    //
                    // db.setTransactionSuccessful();
                    // db.endTransaction();

                    // the column names changed, which is challenging to fix and not really worth
                    // the effort for dev data.
                    super.upgradeTables(db, oldVersion, newVersion);
                }

                Log.d(TAG, "upgraded DB from version " + oldVersion + " to " + newVersion);
            };
        };

        final GenericDBHelper users = new GenericDBHelper(User.class);

        // content://authority/card
        // content://authority/card/1
        addDirAndItemUri(cards, Card.PATH);

        addDirAndItemUri(users, User.PATH);

        final ForeignKeyDBHelper cardmedia = new ForeignKeyDBHelper(Card.class, CardMedia.class,
                CardMedia.CARD);

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
        if (Card.CONTENT_URI.equals(uri)) {
            return NetworkClient.getBaseUrlFromManifest(context) + "postcard/";
        } else {
            return super.getPublicPath(context, uri);
        }
    }

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }
}
