package edu.mit.mobile.android.livingpostcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.ActionBarSherlock.OnPrepareOptionsMenuListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.data.Authorable;

public class MainActivity extends SherlockFragmentActivity implements OnCreateOptionsMenuListener,
        OnOptionsItemSelectedListener, NoAccountFragment.OnLoggedInListener,
        OnPrepareOptionsMenuListener, TabListener {

    NoAccountFragment mNoAccountFragment;

    private static final String TAG_SPLASH = "splash";
    private static final String TAG_NEW = "new";
    private static final String TAG_NEARBY = "nearby";
    private static final String TAG_MY = "my";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mJustLoggedIn;

    private boolean mIsLoggedIn = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTitle("");
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();

        final FragmentManager fm = getSupportFragmentManager();

        if (Authenticator.hasRealAccount(this)) {
            showMainScreen();

            // when there is no account, show a splash page
        } else {
            final Fragment f = fm.findFragmentById(android.R.id.content);

            if (f == null || !(f instanceof NoAccountFragment)) {
                final FragmentTransaction ft = fm.beginTransaction();
                final NoAccountFragment f2 = new NoAccountFragment();
                ft.replace(android.R.id.content, f2, TAG_SPLASH);
                mNoAccountFragment = f2;
                f2.registerOnLoggedInListener(this);
                ft.commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mJustLoggedIn) {
            showMainScreen();
            mJustLoggedIn = false;
        }
    }

    private void showMainScreen() {
        mIsLoggedIn = Authenticator.hasRealAccount(this);
        invalidateOptionsMenu();

        final FragmentManager fm = getSupportFragmentManager();
        final Fragment f = fm.findFragmentById(android.R.id.content);

        if (f != null && f instanceof NoAccountFragment) {
            final FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            ft.remove(f);
            ft.commit();
        }
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText("What's new").setTabListener(this)
                .setTag(TAG_NEW));
        actionBar.addTab(actionBar.newTab().setText("My Postcards").setTabListener(this)
                .setTag(TAG_MY));

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

    private void createNewCard() {

        final Intent intent = new Intent(Intent.ACTION_INSERT, Card.CONTENT_URI);
        startActivity(intent);
    }

    @Override
    public void onLoggedIn() {
        mJustLoggedIn = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.new_card).setVisible(mIsLoggedIn);

        return true;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        final FragmentManager fm = getSupportFragmentManager();

        final String tag = (String) tab.getTag();

        final Fragment f = fm.findFragmentByTag(tag);
        if (f != null) {
            ft.attach(f);
        } else {
            ft.add(android.R.id.content, instantiateFragment(tag), tag);
        }
    }

    private Fragment instantiateFragment(String tag) {
        Fragment f;
        if (TAG_MY.equals(tag)) {
            f = CardListFragment.instantiate(Authorable.getAuthoredBy(Card.CONTENT_URI,
                    Authenticator.getUserUri(this, Authenticator.ACCOUNT_TYPE)));
        } else if (TAG_NEW.equals(tag)) {
            f = CardListFragment.instantiate(Card.CONTENT_URI);
        } else {
            throw new IllegalArgumentException("cannot instantiate fragment for tag " + tag);
        }
        return f;
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        final FragmentManager fm = getSupportFragmentManager();
        final String tag = (String) tab.getTag();
        final Fragment f = fm.findFragmentByTag(tag);
        if (f != null) {
            ft.detach(f);
        }

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }
}
