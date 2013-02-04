package edu.mit.mobile.android.livingpostcards.util;
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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.mit.mobile.android.livingpostcards.BuildConfig;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.sync.AccountSyncService;

/**
 * Version-specific patches to fix issues that were deployed.
 *
 * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
 *
 */
public class Patches {

    private static final String TAG = Patches.class.getSimpleName();

    private static final String PREF_LAST_APPLIED_PATCH = "last_applied_patch";

    public static void checkforAndApplyPatches(Context context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final int lastAppliedPatch = prefs.getInt(PREF_LAST_APPLIED_PATCH, 0);

        final int thisVersion = getAppVersion(context);

        if (lastAppliedPatch == thisVersion) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "applying any patches for version " + thisVersion);
        }

        switch (thisVersion) {
            case 10:
            case 11:
            case 12:
                patchV10V12FixLivingPostcardsDomain(context);
                break;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Applied all patches.");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                prefs.edit().putInt(PREF_LAST_APPLIED_PATCH, thisVersion).commit();
            }
        }).start();
    }

    private static int getAppVersion(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (final NameNotFoundException e) {
            Log.e(TAG, "error getting app version", e);
            return 0;
        }

        return pInfo.versionCode;
    }

    private static void patchV10V12FixLivingPostcardsDomain(Context context) {
        final Account me = Authenticator.getFirstAccount(context);
        if (me != null) {
            AccountSyncService.setApiUrl(context, me, "https://livingpostcards.org/api/");
        }
    }
}
