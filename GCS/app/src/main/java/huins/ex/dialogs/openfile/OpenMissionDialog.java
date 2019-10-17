package huins.ex.dialogs.openfile;

import huins.ex.util.File.IO.MissionReader;
import huins.ex.view.dialogs.OpenFileDialog;

public abstract class OpenMissionDialog extends OpenFileDialog {
	public abstract void waypointFileLoaded(MissionReader reader);

	@Override
	protected FileReader createReader() {
		return new MissionReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		waypointFileLoaded((MissionReader) reader);
	}
}
