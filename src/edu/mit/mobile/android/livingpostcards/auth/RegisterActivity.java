package edu.mit.mobile.android.livingpostcards.auth;

import android.os.Bundle;
import android.util.Log;
import edu.mit.mobile.android.livingpostcards.R;
import edu.mit.mobile.android.locast.accounts.AbsRegisterActivity;

public class RegisterActivity extends AbsRegisterActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected CharSequence getAppName() {
        return getString(R.string.app_name);
    }

    @Override
    protected void onRegisterComplete(Bundle result) {
        super.onRegisterComplete(result);

        Log.d(TAG, result.toString());
    }
}
