package edu.mit.mobile.android.livingpostcards.sync;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import edu.mit.mobile.android.livingpostcards.auth.AuthenticationService;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.livingpostcards.data.CardProvider;
import edu.mit.mobile.android.locast.data.NoPublicPath;
import edu.mit.mobile.android.locast.data.SyncException;
import edu.mit.mobile.android.locast.net.NetworkProtocolException;
import edu.mit.mobile.android.locast.sync.AbsLocastAccountSyncService;
import edu.mit.mobile.android.locast.sync.SyncEngine;

public class AccountSyncService extends AbsLocastAccountSyncService {

    @Override
    public String getAuthority() {
        return CardProvider.AUTHORITY;
    }

    /**
     * Sets the API URL stored in the account info to the desired URL
     *
     * @param context
     * @param account
     * @param desiredUrl
     */
    public static void setApiUrl(Context context, Account account, String desiredUrl) {
        final AccountManager am = AccountManager.get(context);

        am.setUserData(account, AuthenticationService.USERDATA_LOCAST_API_URL, desiredUrl);
    }

    @Override
    public void syncDefaultItems(SyncEngine syncEngine, Account account, Bundle extras,
            ContentProviderClient provider, SyncResult syncResult) throws HttpResponseException,
            RemoteException, SyncException, JSONException, IOException, NetworkProtocolException,
            NoPublicPath, OperationApplicationException, InterruptedException {
        syncEngine.sync(Card.CONTENT_URI, account, extras, provider, syncResult);

    }
}
