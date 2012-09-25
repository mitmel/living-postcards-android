package edu.mit.mobile.android.livingpostcards.auth;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import edu.mit.mobile.android.livingpostcards.data.CardProvider;
import edu.mit.mobile.android.locast.accounts.AbsLocastAuthenticator;

public class Authenticator extends AbsLocastAuthenticator {

    public Authenticator(Context context) {
        super(context);
    }

    public static final String ACCOUNT_TYPE = CardProvider.AUTHORITY;

    public static Account getFirstAccount(Context context) {
        return getFirstAccount(context, ACCOUNT_TYPE);
    }

    public static String getUserUri(Context context) {
        return getUserUri(context, ACCOUNT_TYPE);
    }

    public static boolean hasRealAccount(Context context) {
        return hasRealAccount(context, ACCOUNT_TYPE);
    }

    @Override
    public Intent getAuthenticator(Context context) {
        return new Intent(context, AuthenticatorActivity.class);
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // TODO Auto-generated method stub
        return ACCOUNT_TYPE;
    }

    @Override
    public String getAccountType() {
        return ACCOUNT_TYPE;
    }

    @Override
    public String getAuthTokenType() {
        // TODO Auto-generated method stub
        return ACCOUNT_TYPE;
    }

}
