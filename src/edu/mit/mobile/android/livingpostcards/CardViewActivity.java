package edu.mit.mobile.android.livingpostcards;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.livingpostcards.data.Card;

public class CardViewActivity extends FragmentActivity implements OnClickListener,
        OnCreateOptionsMenuListener, OnOptionsItemSelectedListener, LoaderCallbacks<Cursor> {

    private static final String[] CARD_PROJECTION = new String[] { Card._ID, Card.COL_TITLE,
            Card.COL_TIMING };
    private static final String TAG = CardViewActivity.class.getSimpleName();
    private Uri mCard;
    private CardMediaViewFragment mCardMediaFragment;

    private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);

    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        mSherlock.setContentView(R.layout.activity_card_view);

        mSherlock.getActionBar().setHomeButtonEnabled(true);

        mCard = getIntent().getData();

        findViewById(R.id.add_frame).setOnClickListener(this);

        final FragmentManager fm = getSupportFragmentManager();

        final FragmentTransaction ft = fm.beginTransaction();

        final Fragment f = fm.findFragmentById(R.id.card_media_viewer);
        if (f != null) {
            mCardMediaFragment = (CardMediaViewFragment) f;
        } else {
            mCardMediaFragment = CardMediaViewFragment.newInstance(Card.MEDIA.getUri(mCard));
            ft.replace(R.id.card_media_viewer, mCardMediaFragment);
        }

        getSupportLoaderManager().initLoader(0, null, this);

        ft.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_frame:
                startActivity(new Intent(CameraActivity.ACTION_ADD_PHOTO, mCard));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                return true;

            case android.R.id.home:
                startActivity(new Intent(Intent.ACTION_VIEW, Card.CONTENT_URI));
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSherlock.getMenuInflater().inflate(R.menu.activity_card_view, menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return mSherlock.dispatchOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, mCard, CARD_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (c.moveToFirst()) {
            mSherlock.setTitle(c.getString(c.getColumnIndex(Card.COL_TITLE)));
            final int timing = c.getInt(c.getColumnIndex(Card.COL_TIMING));
            Log.d(TAG, "timing: " + timing);

            mCardMediaFragment.setAnimationTiming(timing);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
