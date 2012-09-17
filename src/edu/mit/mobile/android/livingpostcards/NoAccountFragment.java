package edu.mit.mobile.android.livingpostcards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import edu.mit.mobile.android.livingpostcards.auth.AuthenticatorActivity;

public class NoAccountFragment extends Fragment implements OnClickListener {

    private static final int REQUEST_LOGIN = 100;
    private Button mLogin;

    // private WeakReference<OnLoggedInListener> mOnLoggedInListener;
    private OnLoggedInListener mOnLoggedInListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_no_account, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogin = (Button) view.findViewById(R.id.login);
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                startActivityForResult(new Intent(getActivity(), AuthenticatorActivity.class),
                        REQUEST_LOGIN);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                // if (resultCode == Activity.RESULT_OK && mOnLoggedInListener != null) {
                // final OnLoggedInListener l = mOnLoggedInListener.get();
                // if (l != null) {
                // l.onLoggedIn();
                // }
                // }
                if (resultCode == Activity.RESULT_OK && mOnLoggedInListener != null) {
                    mOnLoggedInListener.onLoggedIn();
                }
                break;
        }
    }

    public void registerOnLoggedInListener(OnLoggedInListener listener) {
        // mOnLoggedInListener = new WeakReference<NoAccountFragment.OnLoggedInListener>(listener);
        mOnLoggedInListener = listener;
    }

    public void unregisterOnLoggedInListener() {
        mOnLoggedInListener = null;
    }

    public static interface OnLoggedInListener {
        public void onLoggedIn();
    }
}
