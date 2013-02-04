package edu.mit.mobile.android.livingpostcards.sync;
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
import android.net.Uri;
import android.util.Log;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.livingpostcards.data.CardMedia;
import edu.mit.mobile.android.locast.data.CastMedia;
import edu.mit.mobile.android.locast.data.SyncException;
import edu.mit.mobile.android.locast.sync.AbsMediaSync;
import edu.mit.mobile.android.locast.sync.LocastSyncService;

public class MediaSyncService extends AbsMediaSync {

    private static final String TAG = MediaSyncService.class.getSimpleName();
    private static final String[] PROJECTION = new String[] { CardMedia._ID,
            CardMedia.COL_MEDIA_URL, CardMedia.COL_PUBLIC_URL, CardMedia.COL_CARD };

    @Override
    public void enqueueUnpublishedMedia() throws SyncException {
        Log.d(TAG, "TODO enqueue unpublished media");
        // TODO
        LocastSyncService.startSync(this, Card.MEDIA.getAll(Card.CONTENT_URI));
    }

    @Override
    public String[] getCastMediaProjection() {
        return ArrayUtils.concat(super.getCastMediaProjection(),
                new String[] { CardMedia.COL_AUTHOR_URI });
    }

    @Override
    public boolean getKeepOffline(Uri castMediaUri, CastMedia castMedia) {
        // by default, don't keep any originals offline.
        return false;
    }

    @Override
    public Account getAccount() {
        return Authenticator.getFirstAccount(this, Authenticator.ACCOUNT_TYPE);
    }

    @Override
    public Uri getTitledItemForCastMedia(Uri castMedia) {
        return CardMedia.getCard(castMedia);
    }

}
