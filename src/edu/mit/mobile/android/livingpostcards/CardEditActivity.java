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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.livingpostcards.DeleteDialogFragment.OnDeleteListener;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.livingpostcards.data.CardMedia;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.locast.data.JsonSyncableItem;
import edu.mit.mobile.android.locast.data.PrivatelyAuthorable;
import edu.mit.mobile.android.locast.sync.LocastSyncService;

public class CardEditActivity extends FragmentActivity implements OnCreateOptionsMenuListener,
        OnOptionsItemSelectedListener, LoaderCallbacks<Cursor>, OnPrepareOptionsMenuListener,
        OnDeleteListener, OnClickListener {

    private static final String[] CARD_PROJECTION = new String[] { Card._ID, Card.COL_TITLE,
            Card.COL_DESCRIPTION, Card.COL_DRAFT, Card.COL_TIMING, Card.COL_AUTHOR_URI,
            Card.COL_PRIVACY };
    private static final String TAG = CardEditActivity.class.getSimpleName();
    public static final String PREF_DIALOG_COLLABORATIVE_SEEN = "CardEditActivity.DIALOG_COLLABORATIVE_SEEN";
    private static final String TAG_DELETE_DIALOG = "delete-dialog";
    private static final String TAG_DIALOG_COLLABORATIVE = "dialog-collaborative";

    private Uri mCard;
    private CardMediaEditFragment mCardViewFragment;

    private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);
    private String mUserUri;
    private boolean mIsEditable;
    private boolean mIsDraft;
    private EditText mTitle;
    private EditText mDescription;
    private final SetCollabDescDialogFragment.OnMarkCollaborativeListener mOnMarkCollabListener = new SetCollabDescDialogFragment.OnMarkCollaborativeListener() {

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onMarkCollaborative() {
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(CardEditActivity.this);

            final boolean neverShowAgain = false;
            if (neverShowAgain) {
                final Editor e = prefs.edit().putBoolean(PREF_DIALOG_COLLABORATIVE_SEEN, true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    e.apply(); // this was introduced in Gingerbread
                } else {
                    e.commit();
                }
            }
            setCollaborative(true);

        }
    };

    private boolean mIsOwner;
    private boolean mIsCollaborative;
    private boolean mSaveOnPause = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        mSherlock.setContentView(R.layout.activity_card_edit);
        final ActionBar ab = mSherlock.getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mTitle = (EditText) findViewById(R.id.title);
        mDescription = (EditText) findViewById(R.id.description);


        final View addFrame = findViewById(R.id.add_frame);

        addFrame.setOnClickListener(this);

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

            // if the dialog has been automatically restored by the system, hook it in.
            final SetCollabDescDialogFragment collab = (SetCollabDescDialogFragment) fm
                    .findFragmentByTag(TAG_DIALOG_COLLABORATIVE);
            if (collab != null) {
                collab.setOnMarkCollaborativeListener(mOnMarkCollabListener);
            }

            mUserUri = Authenticator.getUserUri(this, Authenticator.ACCOUNT_TYPE);

            getSupportLoaderManager().initLoader(0, null, this);

            // if this isn't null, it was saved automatically for us. So hook it back in.
            final DeleteDialogFragment deleteDialog = (DeleteDialogFragment) fm
                    .findFragmentByTag(TAG_DELETE_DIALOG);
            if (deleteDialog != null) {
                deleteDialog.registerOnDeleteListener(this);

            } else if (Intent.ACTION_DELETE.equals(action)) {
                onDeletePostcard();
            }

            ft.commit();
        }
    }

    private void onDeletePostcard() {
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
                onPublish();
                return true;

            case R.id.save:
                onSave();
                return true;

            case R.id.delete:
                onDeletePostcard();
                return true;

            case android.R.id.home: {
                final Intent i = new Intent(Intent.ACTION_VIEW, mCard);
                NavUtils.navigateUpTo(this, i);
            }
                return true;

            case R.id.add_frame:
                // this is conceptually starting it for a result, but the result isn't actually
                // used.
                startActivityForResult(new Intent(CameraActivity.ACTION_ADD_PHOTO, mCard), 0);
                return true;

            case R.id.make_collaborative:
                onMakeCollaborative();
                return true;

            case R.id.make_personal:
                onMakePersonal();
                return true;

            default:
                return false;
        }
    }

    /**
     * Queries the card media to determine if the card media have any contributors other than the
     * owner of the card.
     *
     * @param card
     * @return
     */
    private boolean hasNonOwnerContributors(Uri card) {
        boolean hasNonOwnerContributors = false;
        final String myAuthorUri = Authenticator.getUserUri(this);
        final String[] proj = new String[] { CardMedia.COL_AUTHOR_URI };
        final Cursor media = Card.MEDIA.query(getContentResolver(), card, proj);
        final int cardMediaAuthorCol = media.getColumnIndexOrThrow(CardMedia.COL_AUTHOR_URI);
        try {
            for (media.moveToFirst(); !hasNonOwnerContributors && !media.isAfterLast(); media
                    .moveToNext()) {
                final String cardAuthor = media.getString(cardMediaAuthorCol);
                hasNonOwnerContributors |= !myAuthorUri.equals(cardAuthor);
            }
        } finally {
            media.close();
        }

        return hasNonOwnerContributors;
    }

    public static class SetCollabDescDialogFragment extends DialogFragment implements
            DialogInterface.OnClickListener {
        public interface OnMarkCollaborativeListener {
            public void onMarkCollaborative();
        }

        private OnMarkCollaborativeListener mListener;

        public void setOnMarkCollaborativeListener(OnMarkCollaborativeListener l) {
            mListener = l;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher)
                    .setTitle(R.string.make_collaborative)
                    .setPositiveButton(R.string.make_collaborative, this)
                    .setMessage(R.string.make_collaborative_feature_description)
                    .setNegativeButton(android.R.string.cancel, this).setCancelable(true).create();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mListener = null;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mListener.onMarkCollaborative();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dismiss();
                    break;
            }
        }
    }

    private void setCollaborative(boolean collaborative) {
        final boolean success = Card.setCollaborative(getContentResolver(), mCard, collaborative);
        if (success) {
            if (Constants.DEBUG) {
                Log.d(TAG, mCard + " saved successfully.");
            }
            LocastSyncService.startSync(this, mCard, true);
        }
    }

    private void onMakeCollaborative() {
        if (mIsCollaborative) {
            return;
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final boolean dialogSeen = prefs.getBoolean(PREF_DIALOG_COLLABORATIVE_SEEN, false);

        if (!dialogSeen) {
            final SetCollabDescDialogFragment df = new SetCollabDescDialogFragment();
            df.setOnMarkCollaborativeListener(mOnMarkCollabListener);
            df.show(getSupportFragmentManager(), TAG_DIALOG_COLLABORATIVE);
        } else {
            setCollaborative(true);
            Toast.makeText(this, R.string.notice_make_collaborative_success, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void onMakePersonal() {
        if (!mIsCollaborative) {
            return;
        }

        new AsyncTask<Uri, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Uri... params) {
                boolean madePersonal = false;
                if (!hasNonOwnerContributors(mCard)) {
                    madePersonal = Card.setCollaborative(getContentResolver(), mCard, false);
                }

                return madePersonal;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Toast.makeText(CardEditActivity.this,
                            R.string.notice_make_card_personal_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardEditActivity.this,
                            R.string.notice_cannot_make_card_personal, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(mCard);
    }

    private boolean validate() {
        if (mTitle.length() == 0) {
            mTitle.setError(getString(R.string.postcard_edit_title_empty));
            mTitle.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSaveOnPause) {
            save();
        }
    }

    /**
     * Saves the card without any validation.
     *
     * @return true if save was successful.
     */
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

    private void onSave() {
        if (!validate()) {
            return;
        }

        if (save()) {
            finish();
        } else {
            Toast.makeText(this, R.string.err_publish_fail, Toast.LENGTH_LONG).show();
        }
    }

    protected void onPublish() {
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

        // only the owner can do the following.
        menu.findItem(R.id.delete).setVisible(mIsOwner);
        menu.findItem(R.id.delete_photos).setVisible(mIsOwner);
        menu.findItem(R.id.publish).setVisible(mIsOwner && mIsDraft);
        menu.findItem(R.id.save).setVisible(mIsOwner && !mIsDraft);

        final MenuItem makeCollab = menu.findItem(R.id.make_collaborative);
        makeCollab.setVisible(mIsOwner && !mIsCollaborative);

        final MenuItem makePersonal = menu.findItem(R.id.make_personal);
        makePersonal.setVisible(mIsOwner && mIsCollaborative);

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
            findViewById(R.id.add_frame).setVisibility(mIsEditable ? View.VISIBLE : View.GONE);
            mIsOwner = mUserUri.equals(c.getString(c.getColumnIndexOrThrow(Card.COL_AUTHOR_URI)));

            final String privacy = c.getString(c.getColumnIndexOrThrow(Card.COL_PRIVACY));
            mIsCollaborative = Card.PRIVACY_PUBLIC.equals(privacy);

            mTitle.setEnabled(mIsEditable && mIsOwner);
            mDescription.setEnabled(mIsEditable && mIsOwner);

            mIsDraft = JsonSyncableItem.isDraft(c);
            mSherlock.dispatchInvalidateOptionsMenu();
            final int timing = c.getInt(c.getColumnIndexOrThrow(Card.COL_TIMING));
            mCardViewFragment.setAnimationTiming(timing);
            mSaveOnPause = true;
        } else {
            mSaveOnPause = false;
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mSaveOnPause = false;
    }

    @Override
    public void onDelete(Uri item, boolean deleted) {
        if (mCard.equals(item) && deleted) {
            mSaveOnPause = false;
            setResult(RESULT_OK);
            // no need to call finish, as the loader will automatically reload, which will result in
            // no data being loaded, which will then call finish()

        } else if (Intent.ACTION_DELETE.equals(getIntent().getAction()) && !deleted) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_frame:
                // this is conceptually starting it for a result, but the result isn't actually
                // used.
                startActivityForResult(new Intent(CameraActivity.ACTION_ADD_PHOTO, mCard), 0);
                break;

            default:
                break;
        }
    }
}
