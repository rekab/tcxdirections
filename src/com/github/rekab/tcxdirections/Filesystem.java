package com.github.rekab.tcxdirections;

import java.io.File;

import android.os.Environment;

public final class Filesystem {

	public static final String SD_SUBDIR_NAME = "gpx";

	/** Create the storage directory and return the directory. */
	public static File getStorageDirectory() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/" + SD_SUBDIR_NAME);
        dir.mkdirs();
        return dir;
	}

}
