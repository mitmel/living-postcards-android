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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnLoggedInListener) {
            registerOnLoggedInListener(mOnLoggedInListener);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterOnLoggedInListener();
    }

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


    /**
     * Register a callback for when the login succeeded. If your activity implements this interface,
     * you don't need to register it here - it'll be automatically registered when it attaches.
     * 
     * @param listener
     */
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
