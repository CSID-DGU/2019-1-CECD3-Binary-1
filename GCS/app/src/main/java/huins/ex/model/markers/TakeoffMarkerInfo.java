package huins.ex.model.markers;

import huins.ex.R;
import huins.ex.model.MissionItemProxy;

/**
 * This implements the marker source for a takeoff mission item.
 */
class TakeoffMarkerInfo extends MissionItemMarkerInfo {

	protected TakeoffMarkerInfo(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return R.drawable.ic_wp_takeof_selected;
	}

	@Override
	protected int getIconResource() {
		return R.drawable.ic_wp_takeof_selected;
	}
}
