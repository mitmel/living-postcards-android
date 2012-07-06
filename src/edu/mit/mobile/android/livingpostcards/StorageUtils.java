package edu.mit.mobile.android.livingpostcards;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Environment;

public class StorageUtils {
	public static final String EXTERNAL_PICTURE_SUBDIR = "livingpostcards";
	public static final File EXTERNAL_PICTURES_DIR = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			EXTERNAL_PICTURE_SUBDIR);

	/**
	 * Gets the most recent image in the {@link #EXTERNAL_PICTURES_DIR}.
	 *
	 * @return
	 */
	public static File getMostRecentPicture() {
		if (!EXTERNAL_PICTURES_DIR.exists()) {
			return null;
		}

		final File[] pictures = EXTERNAL_PICTURES_DIR.listFiles(new FileFilter() {

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

				return new Long(lhs.lastModified()).compareTo(rhs.lastModified());
			}
		});

		return pictures[pictures.length - 1];
	}
}
