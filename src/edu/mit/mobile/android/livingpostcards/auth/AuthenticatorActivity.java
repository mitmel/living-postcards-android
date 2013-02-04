package edu.mit.mobile.android.livingpostcards.auth;
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

import android.accounts.Account;
import android.content.Intent;
import edu.mit.mobile.android.livingpostcards.R;
import edu.mit.mobile.android.livingpostcards.data.CardProvider;
import edu.mit.mobile.android.locast.accounts.AbsLocastAuthenticatorActivity;

public class AuthenticatorActivity extends AbsLocastAuthenticatorActivity {

    @Override
    protected CharSequence getAppName() {
        return getString(R.string.app_name);
    }

    @Override
    protected Account createAccount(String username) {
        return new Account(username, Authenticator.ACCOUNT_TYPE);
    }

    @Override
    protected String getAuthority() {
        return CardProvider.AUTHORITY;
    }

    @Override
    protected Intent getSignupIntent() {
        return new Intent(this, RegisterActivity.class);
    }

    @Override
    protected String getAccountType() {
        return Authenticator.ACCOUNT_TYPE;
    }

    @Override
    protected String getAuthtokenType() {
        return Authenticator.ACCOUNT_TYPE;
    }

    @Override
    protected boolean isEmailAddressLogin() {
        return false;
    }
}
