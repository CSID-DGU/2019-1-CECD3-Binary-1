package huins.ex.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DirectoryPath {


	public static String getPrivateDataPath(Context context){
		File dataDir = context.getExternalFilesDir(null);
		return dataDir.getAbsolutePath();
	}

	static public String getPublicDataPath() {
		String root = Environment.getExternalStorageDirectory().getPath();
		return (root + "/Blueye/");
	}
	static public String getParametersPath() {
		return getPublicDataPath() + "/Parameters/";
	}

	static public String getWaypointsPath() {
		return getPublicDataPath() + "/Waypoints/";
	}

	static public String getMediaPath() {
		return getPublicDataPath() + "/media/";
	}

	static public String getMapsPath() {
		return getPublicDataPath() + "/Maps/";
	}

	public static String getLogCatPath(Context context) {
		return getPrivateDataPath(context) + "/LogCat/";
	}

	public static String getPublicDataPath(Context context){
		final String root = Environment.getExternalStorageDirectory().getPath();
		return root + "/BlueyeService/";
	}

	static public File getTLogPath(Context context, String appId) {
		File f = new File(getPrivateDataPath(context) + "/tlogs/" + appId);
		if(!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	static public File getTLogSentPath(Context context, String appId) {
		File f = new File(getTLogPath(context, appId) + "/sent/");
		if(!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	public static String getCameraInfoPath(Context context) {
		return getPublicDataPath(context) + "/CameraInfo/";
	}
}