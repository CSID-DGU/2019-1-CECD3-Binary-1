package huins.ex.view.widgets.checklist;

import android.content.Context;

import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Battery;
import huins.ex.proto.property.Gps;
import huins.ex.proto.property.State;
import huins.ex.view.activities.GCSBaseApp;

public class CheckListSysLink {
    private Context context;
	private Flight flight;

	public CheckListSysLink(Context context, Flight flight) {
        this.context = context;
		this.flight = flight;
	}

	public void getSystemData(CheckListItem mListItem, String mSysTag) {
		if (mSysTag == null)
			return;

		Battery batt = flight.getAttribute(AttributeType.BATTERY);
		if (batt != null) {
			if (mSysTag.equalsIgnoreCase("SYS_BATTREM_LVL")) {
				mListItem.setSys_value(batt.getBatteryRemain());
			} else if (mSysTag.equalsIgnoreCase("SYS_BATTVOL_LVL")) {
				mListItem.setSys_value(batt.getBatteryVoltage());
			} else if (mSysTag.equalsIgnoreCase("SYS_BATTCUR_LVL")) {
				mListItem.setSys_value(batt.getBatteryCurrent());
			}
		}

		Gps gps = flight.getAttribute(AttributeType.GPS);
		if (gps != null) {
			if (mSysTag.equalsIgnoreCase("SYS_GPS3D_LVL")) {
				mListItem.setSys_value(gps.getSatellitesCount());
			}
		}

		State state = flight.getAttribute(AttributeType.STATE);
		if (state != null) {
			if (mSysTag.equalsIgnoreCase("SYS_ARM_STATE")) {
				mListItem.setSys_activated(state.isArmed());
			} else if (mSysTag.equalsIgnoreCase("SYS_FAILSAFE_STATE")) {
				mListItem.setSys_activated(state.isWarning());
			}
		}

		if (mSysTag.equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			mListItem.setSys_activated(flight.IsConnected());
		}
	}

	public void setSystemData(CheckListItem checkListItem) {

		if (checkListItem.getSys_tag() == null)
			return;

		if (checkListItem.getSys_tag().equalsIgnoreCase("SYS_CONNECTION_STATE")) {
			doSysConnect(checkListItem);

		} else if (checkListItem.getSys_tag().equalsIgnoreCase("SYS_ARM_STATE")) {
			doSysArm(checkListItem);

		}
	}

	private void doSysArm(CheckListItem checkListItem) {
        final State flightState = flight.getAttribute(AttributeType.STATE);
		if (flightState.isConnected()) {
			if (checkListItem.isSys_activated() && !flightState.isArmed()) {
				flight.arm(true);
			} else {
				flight.arm(false);
			}
		}
	}

	private void doSysConnect(CheckListItem checkListItem) {
		boolean activated = checkListItem.isSys_activated();
		boolean connected = flight.IsConnected();
		if (activated != connected) {
			if (connected)
				GCSBaseApp.disconnectFromFlight(context);
			else
				GCSBaseApp.connectToFlight(context);
		}
	}

}
