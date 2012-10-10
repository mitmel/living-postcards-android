package edu.mit.mobile.android.livingpostcards;

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
