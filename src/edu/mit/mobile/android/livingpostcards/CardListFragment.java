package edu.mit.mobile.android.livingpostcards;
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

import java.io.IOException;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.imagecache.SimpleThumbnailCursorAdapter;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.data.PrivatelyAuthorable;
import edu.mit.mobile.android.locast.sync.LocastSyncService;

public class CardListFragment extends ListFragment implements LoaderCallbacks<Cursor>,
        OnItemClickListener, OnClickListener {

    private static final String[] FROM = { Card.COL_TITLE, Card.COL_AUTHOR, Card.COL_COVER_PHOTO,
            Card.COL_THUMBNAIL, Card.COL_DRAFT };
    private static final int[] TO = { R.id.title, R.id.author, R.id.card_media_thumbnail,
            R.id.card_media_thumbnail };

    private static final String[] PROJECTION = ArrayUtils.concat(new String[] { Card._ID,
            Card.COL_PRIVACY, Card.COL_AUTHOR_URI, Card.COL_WEB_URL }, FROM);

    private SimpleThumbnailCursorAdapter mAdapter;

    ImageCache mImageCache;

    public static final String ARG_CARD_DIR_URI = "uri";

    private Uri mCards = Card.CONTENT_URI;
    private float mDensity;
    private static final String TAG = CardListFragment.class.getSimpleName();
    private static final int[] IMAGE_IDS = new int[] { R.id.card_media_thumbnail };

    public CardListFragment() {
        super();
    }

    public static CardListFragment instantiate(Uri cardDir) {
        final Bundle b = new Bundle();
        b.putParcelable(ARG_CARD_DIR_URI, cardDir);
        final CardListFragment f = new CardListFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            final Uri newUri = args.getParcelable(ARG_CARD_DIR_URI);
            mCards = newUri != null ? newUri : mCards;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.card_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.new_card).setOnClickListener(this);

        // add a footer so that there's enough room for the content even with the red button at the
        // bottom
        final ListView lv = getListView();
        lv.addFooterView(
                getLayoutInflater(savedInstanceState).inflate(R.layout.scroll_footer, lv, false),
                null, false);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mImageCache = ImageCache.getInstance(getActivity());

        mAdapter = new SimpleThumbnailCursorAdapter(getActivity(), R.layout.card_list_item, null,
                FROM, TO, IMAGE_IDS, 0) {
            @Override
            public void bindView(View v, Context context, Cursor c) {
                super.bindView(v, context, c);
                ((TextView) v.findViewById(R.id.title)).setText(Card.getTitle(context, c));
            }
        };

        setListAdapter(new ImageLoaderAdapter(getActivity(), mAdapter, mImageCache, IMAGE_IDS, 133,
                100, ImageLoaderAdapter.UNIT_DIP));

        getListView().setOnItemClickListener(this);

        getLoaderManager().initLoader(0, null, this);
        LocastSyncService.startExpeditedAutomaticSync(getActivity(), mCards.buildUpon().query(null)
                .build());
        registerForContextMenu(getListView());

        mDensity = getActivity().getResources().getDisplayMetrics().density;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(getActivity(), mCards, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        mAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Uri item = ContentUris.withAppendedId(mCards, id);
        final Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);
        if (Card.isDraft(c)) {
            startActivity(new Intent(Intent.ACTION_EDIT, item));
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, item));
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.activity_card_view, menu);

        final Cursor c = mAdapter.getCursor();
        if (c == null) {
            return;
        }

        final String myUserUri = Authenticator.getUserUri(getActivity());

        final boolean isEditable = PrivatelyAuthorable.canEdit(myUserUri, c);

        menu.findItem(R.id.delete).setVisible(isEditable);
        menu.findItem(R.id.edit).setVisible(isEditable);

        menu.setHeaderTitle(Card.getTitle(getActivity(), c));
        Drawable icon;
        try {
            String iconUrl = c.getString(c.getColumnIndexOrThrow(Card.COL_COVER_PHOTO));
            if (iconUrl == null || iconUrl.length() == 0) {
                iconUrl = c.getString(c.getColumnIndexOrThrow(Card.COL_THUMBNAIL));
            }
            icon = mImageCache.loadImage(0, Uri.parse(iconUrl), (int) (133 * mDensity),
                    (int) (100 * mDensity));

            if (icon != null) {
                menu.setHeaderIcon(new InsetDrawable(icon, (int) (5 * mDensity)));
            }
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void send(Cursor c) {
        final String mWebUrl = c.getString(c.getColumnIndexOrThrow(Card.COL_WEB_URL));
        if (mWebUrl == null) {
            Toast.makeText(getActivity(), R.string.err_share_intent_no_web_url_editable,
                    Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(Card.createShareIntent(getActivity(), mWebUrl,
                Card.getTitle(getActivity(), c)));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (final ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        final Uri card = ContentUris.withAppendedId(mCards, info.id);

        switch (item.getItemId()) {
            case R.id.share:
                send(mAdapter.getCursor());
                return true;

            case R.id.edit:
                startActivity(new Intent(Intent.ACTION_EDIT, card));
                return true;

            case R.id.delete:
                startActivity(new Intent(Intent.ACTION_DELETE, card));
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private void createNewCard() {

        final Intent intent = new Intent(Intent.ACTION_INSERT, Card.CONTENT_URI);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_card:
                createNewCard();
                break;

            default:
                break;
        }

    }
}
