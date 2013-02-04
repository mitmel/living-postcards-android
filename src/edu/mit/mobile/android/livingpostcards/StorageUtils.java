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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.os.Environment;

public class StorageUtils {
    public static File getExternalPictureDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    /**
     * Gets the most recent image in the {@link #EXTERNAL_PICTURES_DIR}.
     *
     * @return
     */
    public static File getMostRecentPicture(Context context) {
        final File externalPicturesDir = getExternalPictureDir(context);
        if (!externalPicturesDir.exists()) {
            return null;
        }

        final File[] pictures = externalPicturesDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jpg");
            }
        });

        if (pictures.length == 0) {
            return null;
        }

        Arrays.sort(pictures, new Comparator<File>() {

            @Override
            public int compare(File lhs, File rhs) {

                return Long.valueOf(lhs.lastModified()).compareTo(rhs.lastModified());
            }
        });

        return pictures[pictures.length - 1];
    }
}
