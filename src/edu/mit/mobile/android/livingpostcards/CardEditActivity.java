package edu.mit.mobile.android.livingpostcards;

import android.content.ContentValues;
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
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.livingpostcards.DeleteDialogFragment.OnDeleteListener;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.locast.data.JsonSyncableItem;
import edu.mit.mobile.android.locast.data.PrivatelyAuthorable;
import edu.mit.mobile.android.locast.sync.LocastSyncService;

public class CardEditActivity extends FragmentActivity implements OnCreateOptionsMenuListener,
        OnOptionsItemSelectedListener, LoaderCallbacks<Cursor>, OnPrepareOptionsMenuListener,
        OnDeleteListener {

    private static final String[] CARD_PROJECTION = new String[] { Card._ID, Card.COL_TITLE,
            Card.COL_DESCRIPTION, Card.COL_DRAFT, Card.COL_TIMING, Card.COL_AUTHOR_URI,
            Card.COL_PRIVACY };
    private static final String TAG = CardEditActivity.class.getSimpleName();
    private static final String TAG_DELETE_DIALOG = "delete-dialog";
    private Uri mCard;
    private CardMediaEditFragment mCardViewFragment;

    private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);
    private String mUserUri;
    private boolean mIsEditable;
    private boolean mIsDraft;
    private EditText mTitle;
    private EditText mDescription;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        mSherlock.setContentView(R.layout.activity_card_edit);

        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);
        mSherlock.getActionBar().setHomeButtonEnabled(true);

        mCard = getIntent().getData();
        final String action = getIntent().getAction();

        final FragmentManager fm = getSupportFragmentManager();

        if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_DELETE.equals(action)) {
            final FragmentTransaction ft = fm.beginTransaction();
            final Fragment f = fm.findFragmentById(R.id.card_edit_fragment);
            if (f != null) {
                mCardViewFragment = (CardMediaEditFragment) f;
            } else {
                mCardViewFragment = CardMediaEditFragment.newInstance(Card.MEDIA.getUri(mCard));
                ft.replace(R.id.card_edit_fragment, mCardViewFragment);
            }

            mUserUri = Authenticator.getUserUri(this, Authenticator.ACCOUNT_TYPE);

            getSupportLoaderManager().initLoader(0, null, this);

            // if this isn't null, it was saved automatically for us. So hook it back in.
            final DeleteDialogFragment deleteDialog = (DeleteDialogFragment) fm
                    .findFragmentByTag(TAG_DELETE_DIALOG);
            if (deleteDialog != null) {
                deleteDialog.registerOnDeleteListener(this);

            } else if (Intent.ACTION_DELETE.equals(action)) {
                showDeleteDialog();
            }

            ft.commit();
        }
    }

    private void showDeleteDialog() {
        final DeleteDialogFragment del = DeleteDialogFragment.newInstance(mCard,
                getText(R.string.delete_postcard),
                getText(R.string.postcard_edit_delete_confirm_message));
        del.registerOnDeleteListener(this);
        del.show(getSupportFragmentManager(), TAG_DELETE_DIALOG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.publish:
                publish();
                return true;

            case R.id.save:
                saveButton();
                return true;

            case R.id.delete:
                showDeleteDialog();
                return true;

            case android.R.id.home:
                startActivity(new Intent(Intent.ACTION_VIEW, Card.CONTENT_URI));
                return true;

            case R.id.add_frame:
                // this is conceptually starting it for a result, but the result isn't actually
                // used.
                startActivityForResult(new Intent(CameraActivity.ACTION_ADD_PHOTO, mCard), 0);
                return true;

            default:
                return false;
        }
    }

    private boolean validate() {
        if (mTitle.length() == 0) {
            mTitle.setError(getString(R.string.postcard_edit_title_empty));
            mTitle.requestFocus();
            return false;
        }

        return true;
    }

    private boolean save() {

        final ContentValues cv = new ContentValues();
        cv.put(Card.COL_DRAFT, mIsDraft);
        cv.put(Card.COL_TITLE, mTitle.getText().toString());
        cv.put(Card.COL_DESCRIPTION, mDescription.getText().toString());
        cv.put(Card.COL_TIMING, mCardViewFragment.getAnimationTiming());

        final int updated = getContentResolver().update(mCard, cv, null, null);

        final boolean success = updated == 1;

        if (success) {
            if (Constants.DEBUG) {
                Log.d(TAG, mCard + " saved successfully.");
            }
            LocastSyncService.startSync(this, mCard, true);
        }
        return success;
    }

    private void saveButton() {
        if (!validate()) {
            return;
        }

        if (save()) {
            finish();
        } else {
            Toast.makeText(this, R.string.err_publish_fail, Toast.LENGTH_LONG).show();
        }
    }

    protected void publish() {
        if (!validate()) {
            return;
        }

        mIsDraft = false;

        if (save()) {
            LocastSyncService.startSync(this, mCard, true);
            finish();
            Toast.makeText(this, R.string.notice_publish_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.err_publish_fail, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSherlock.getMenuInflater().inflate(R.menu.activity_card_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add_frame).setVisible(mIsEditable);
        menu.findItem(R.id.delete).setVisible(mIsEditable);
        menu.findItem(R.id.publish).setVisible(mIsDraft && mIsEditable);
        menu.findItem(R.id.save).setVisible(!mIsDraft && mIsEditable);
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
            mTitle.setText(c.getString(c.getColumnIndex(Card.COL_TITLE)));
            mDescription.setText(c.getString(c.getColumnIndex(Card.COL_DESCRIPTION)));

            mIsEditable = PrivatelyAuthorable.canEdit(mUserUri, c);

            mTitle.setEnabled(mIsEditable);
            mDescription.setEnabled(mIsEditable);

            mIsDraft = JsonSyncableItem.isDraft(c);
            mSherlock.dispatchInvalidateOptionsMenu();
            final int timing = c.getInt(c.getColumnIndexOrThrow(Card.COL_TIMING));
            mCardViewFragment.setAnimationTiming(timing);
        } else {
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    @Override
    public void onDelete(Uri item, boolean deleted) {
        if (mCard.equals(item) && deleted) {
            setResult(RESULT_OK);
            // no need to call finish, as the loader will automatically reload, which will result in
            // no data being loaded, which will then call finish()

        } else if (Intent.ACTION_DELETE.equals(getIntent().getAction()) && !deleted) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
