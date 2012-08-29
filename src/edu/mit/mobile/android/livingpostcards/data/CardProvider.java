package edu.mit.mobile.android.livingpostcards.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.SimpleContentProvider;

public class CardProvider extends SimpleContentProvider {

    public static final String AUTHORITY = "edu.mit.mobile.android.livingpostcards";

    public static final int VERSION = 2;

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
                            + "' ADD COLUMN timing INTEGER NOT NULL DEFAULT 300");
                }

                if (oldVersion < 3) {

                }

                Log.d(TAG, "upgraded DB from version " + oldVersion + " to " + newVersion);
            };
        };

        final GenericDBHelper users = new GenericDBHelper(User.class) {
            @Override
            public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                // do nothing so we don't drop the tables
            }
        };

        // content://authority/card
        // content://authority/card/1
        addDirAndItemUri(cards, Card.PATH);

        addDirAndItemUri(users, User.PATH);

        final ForeignKeyDBHelper cardmedia = new ForeignKeyDBHelper(Card.class, CardMedia.class,
                CardMedia.CARD) {
            @Override
            public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                // do nothing so we don't drop the tables
            }
        };

        // content://authority/card/1/media
        // content://authority/card/1/media/1
        addChildDirAndItemUri(cardmedia, Card.PATH, CardMedia.PATH);

    }
}
