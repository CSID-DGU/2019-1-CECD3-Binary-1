package huins.ex.util.File;

import android.util.Log;

import java.io.FileOutputStream;

import huins.ex.proto.mission.Mission;
import huins.ex.util.FileList;
import huins.ex.util.FileStream;
import huins.ex.util.ParcelableUtils;

/**
 * Write a mission to file.
 */
public class MissionWriter {
	private static final String TAG = MissionWriter.class.getSimpleName();

	public static boolean write(Mission mission) {
		return write(mission, FileStream.getWaypointFilename("waypoints"));
	}

	public static boolean write(Mission mission, String filename) {
		try {
			if (!FileStream.isExternalStorageAvailable())
				return false;

			if (!filename.endsWith(FileList.WAYPOINT_FILENAME_EXT)) {
				filename += FileList.WAYPOINT_FILENAME_EXT;
			}

			final FileOutputStream out = FileStream.getWaypointFileStream(filename);
            byte[] missionBytes = ParcelableUtils.marshall(mission);
            out.write(missionBytes);
			out.close();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
		return true;
	}
}
