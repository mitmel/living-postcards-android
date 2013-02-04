package edu.mit.mobile.android.livingpostcards.data;
/*
 * Copyright (C) 2012-2013  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.accounts.Account;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import edu.mit.mobile.android.content.DBSortOrder;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
import edu.mit.mobile.android.locast.data.Authorable;
import edu.mit.mobile.android.locast.data.ImageContent;
import edu.mit.mobile.android.locast.data.JsonSyncableItem;
import edu.mit.mobile.android.locast.data.MediaProcessingException;
import edu.mit.mobile.android.locast.data.SyncMap;

@UriPath(CardMedia.PATH)
@DBSortOrder(CardMedia.SORT_DEFAULT)
public class CardMedia extends ImageContent implements Authorable.Columns {

    public CardMedia(Cursor c) {
        super(c);
    }

    public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.edu.mit.mobile.android.livingpostcards.cardmedia";
    public static final String TYPE_ITEM = "vnd.android.cursor.dir/vnd.edu.mit.mobile.android.livingpostcards.cardmedia";

    @DBForeignKeyColumn(parent = Card.class)
    public static final String COL_CARD = "card";

    public static final Uri getCard(Uri cardMedia) {
        if (cardMedia.getLastPathSegment().matches("\\d+")) {
            return ProviderUtils.removeLastPathSegments(cardMedia, 2);
        } else {
            return ProviderUtils.removeLastPathSegment(cardMedia);
        }
    }

    public static final String SORT_DEFAULT = COL_CREATED_DATE + " ASC";

    public static final String PATH = "media";


    /**
     * Creates a new card with a random UUID
     *
     * @param cr
     * @param title
     * @return
     */
    public static ContentValues createNewCardMedia(Context context, Account account) {

        final ContentValues cv = JsonSyncableItem.newContentItem();

        Authorable.putAuthorInformation(context, account, cv);

        return cv;
    }

    public static CastMediaInfo addMediaToCard(Context context, Account account, Uri cardMedia,
            Uri content) throws MediaProcessingException {
        final ContentValues cv = CardMedia.createNewCardMedia(context, account);

        final CastMediaInfo cmi = CardMedia.addMedia(context, account, cardMedia, content, cv);

        return cmi;
    }

    public static class ItemSyncMap extends ImageContent.ItemSyncMap {
        /**
         *
         */
        private static final long serialVersionUID = 6879723724633413905L;

        public ItemSyncMap() {
            super();

            putAll(Authorable.SYNC_MAP);
            put("_title", new SyncLiteral("title", "untitled"));
        }
    };

    @Override
    public SyncMap getSyncMap() {
        return SYNC_MAP;
    }

    public static final ItemSyncMap SYNC_MAP = new ItemSyncMap();

    @Override
    public Uri getContentUri() {
        return Card.MEDIA.getUri(ContentUris.withAppendedId(Card.CONTENT_URI,
                getLong(getColumnIndexOrThrow(COL_CARD))));
    }
}
