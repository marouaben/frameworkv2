package ets.genielog.appwatcher_3;

import android.content.pm.PackageInfo;

public interface SelectedApkListener {

	
	public void notifyApkChange(PackageInfo apk, boolean ismodel);
}
