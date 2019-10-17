package huins.ex.model.markers;

import huins.ex.R;
import huins.ex.model.MissionItemProxy;

/**
 * This implements the marker source for the loiter mission item.
 */
class LoiterMarkerInfo extends MissionItemMarkerInfo {
	protected LoiterMarkerInfo(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return R.drawable.ic_wp_loiter_selected;
	}

	@Override
	protected int getIconResource() {
		return R.drawable.ic_wp_circle_cw;
	}
}
