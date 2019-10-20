package huins.ex.util.File;

import android.util.Log;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import huins.ex.proto.mission.Mission;
import huins.ex.util.DirectoryPath;
import huins.ex.util.FileList;
import huins.ex.util.FileStream;
import huins.ex.util.ParcelableUtils;
import huins.ex.view.dialogs.OpenFileDialog;

/**
 * Read a mission from a file.
 */
public class MissionReader implements OpenFileDialog.FileReader {

    private static final String TAG = MissionReader.class.getSimpleName();

	private Mission mission = new Mission();

	public boolean openMission(String file) {
		if (!FileStream.isExternalStorageAvailable()) {
			return false;
		}
		try {
			final FileInputStream in = new FileInputStream(file);
            Map<byte[], Integer> bytesList = new LinkedHashMap<byte[], Integer>();
            int length = 0;
            while(in.available() > 0){
                byte[] missionBytes = new byte[2048];
                int bufferSize = in.read(missionBytes);
                bytesList.put(missionBytes, bufferSize);
                length += bufferSize;
            }

            ByteBuffer fullBuffer = ByteBuffer.allocate(length);
            for(Map.Entry<byte[], Integer> entry : bytesList.entrySet()){
                fullBuffer.put(entry.getKey(), 0, entry.getValue());
            }

            this.mission = ParcelableUtils.unmarshall(fullBuffer.array(), 0, length, Mission.CREATOR);
			in.close();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}

		return true;
	}

	public Mission getMission() {
		return mission;
	}

	@Override
	public String getPath() {
		return DirectoryPath.getWaypointsPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getWaypointFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openMission(file);
	}
}