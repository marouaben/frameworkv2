package ets.genielog.appwatcher_3.profiling.util;

import android.os.Environment;

public class Config {

	public static final String TAG_APP = "Appwatcher_3";

	public static final String PREF_PROFILING_NAME = "profiling";

	public static final String LIB_PROFILING_NAME = "profiling";

	public static final boolean DEBUG_PROFILING = true;

	public static final String APP_PATH = Environment
			.getExternalStorageDirectory().getPath() + "AppWatcher_3/";
	
	public static final String PROCESS_PATH = APP_PATH + "cpu_";
}
