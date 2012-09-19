package edu.mit.mobile.android.livingpostcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;

public class MainActivity extends FragmentActivity implements OnCreateOptionsMenuListener,
        OnOptionsItemSelectedListener, NoAccountFragment.OnLoggedInListener,
        OnPrepareOptionsMenuListener {

    private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);

    NoAccountFragment mNoAccountFragment;

    private boolean mJustLoggedIn;

    private boolean mIsLoggedIn = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTitle("");
        super.onCreate(savedInstanceState);
        mSherlock.setContentView(R.layout.activity_main);

        final FragmentManager fm = getSupportFragmentManager();

        final FragmentTransaction ft = fm.beginTransaction();

        final Fragment f = fm.findFragmentById(R.id.main_fragment);

        if (Authenticator.hasRealAccount(this)) {
            if (f == null || !(f instanceof CardListFragment)) {
                final CardListFragment f2 = new CardListFragment();
                ft.replace(R.id.main_fragment, f2);
            }
            mIsLoggedIn = true;
        } else {
            if (f == null || !(f instanceof NoAccountFragment)) {
                final NoAccountFragment f2 = new NoAccountFragment();
                ft.replace(R.id.main_fragment, f2);
                mNoAccountFragment = f2;
                f2.registerOnLoggedInListener(this);
            }
        }
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mJustLoggedIn) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.replace(R.id.main_fragment, new CardListFragment());
            ft.commit();
            mJustLoggedIn = false;
            mIsLoggedIn = true;

            mSherlock.dispatchInvalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSherlock.getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return mSherlock.dispatchOptionsItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_card:
                createNewCard();

                return true;
        }
        return false;
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        mSherlock.dispatchTitleChanged(title, color);
        super.onTitleChanged(title, color);
    }

    private void createNewCard() {

        final Intent intent = new Intent(Intent.ACTION_INSERT, Card.CONTENT_URI);
        startActivity(intent);
    }

    @Override
    public void onLoggedIn() {
        mJustLoggedIn = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.new_card).setVisible(mIsLoggedIn);

        return true;
    }
}
