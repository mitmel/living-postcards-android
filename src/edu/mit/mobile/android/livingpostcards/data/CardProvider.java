package edu.mit.mobile.android.livingpostcards.data;

import edu.mit.mobile.android.content.ForeignKeyDBHelper;
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.SimpleContentProvider;

public class CardProvider extends SimpleContentProvider {

    public static final String AUTHORITY = "edu.mit.mobile.android.livingpostcards";

    public static final int VERSION = 1;

    public CardProvider() {
        super(AUTHORITY, VERSION);

        final GenericDBHelper cards = new GenericDBHelper(Card.class);

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

}
