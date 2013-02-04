package edu.mit.mobile.android.livingpostcards.app;
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

import edu.mit.mobile.android.livingpostcards.util.Patches;
import edu.mit.mobile.android.locast.app.LocastApplication;

public class LivingPostcardsApplication extends LocastApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Patches.checkforAndApplyPatches(this);
    }
}
