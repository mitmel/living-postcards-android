package edu.mit.mobile.android.livingpostcards.auth;

import android.content.Context;
import android.content.Intent;
import edu.mit.mobile.android.livingpostcards.data.CardProvider;
import edu.mit.mobile.android.locast.accounts.AbsLocastAuthenticator;

public class Authenticator extends AbsLocastAuthenticator {

    public Authenticator(Context context) {
        super(context);
    }

    public static final String ACCOUNT_TYPE = CardProvider.AUTHORITY;

    @Override
    public Intent getAuthenticator(Context context) {
        return new Intent(context, AuthenticatorActivity.class);
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // TODO Auto-generated method stub
        return "kittens";
    }

}
