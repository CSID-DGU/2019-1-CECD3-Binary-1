package huins.ex.core.Flight.variables;

import huins.ex.com.MAVLink.enums.MAV_TYPE;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.firmware.FirmwareType;
import huins.ex.core.model.Flight;

public class Type extends FlightVariable implements FlightInterfaces.OnFlightListener{

    private static final int DEFAULT_TYPE = MAV_TYPE.MAV_TYPE_GENERIC;

	private int type = DEFAULT_TYPE;
	private String firmwareVersion = null;

	public Type(Flight mflight) {
		super(mflight);
        mflight.addFlightListener(this);
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			mflight.notifyFlightEvent(FlightEventsType.TYPE);
			mflight.loadVehicleProfile();
		}
	}

	public int getType() {
		return type;
	}

	public FirmwareType getFirmwareType() {
		if (mflight.getMavClient().isConnected()) {
			switch (this.type) {

			case MAV_TYPE.MAV_TYPE_FIXED_WING:
				return FirmwareType.ARDU_PLANE;

			case MAV_TYPE.MAV_TYPE_GENERIC:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_COAXIAL:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
				return FirmwareType.ARDU_COPTER;

			case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
			case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
				return FirmwareType.ARDU_ROVER;

			default:
				// unsupported - fall thru to offline condition
			}
		}
		return mflight.getPreferences().getVehicleType(); // offline or
															// unsupported
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String message) {
        if(firmwareVersion == null || !firmwareVersion.equals(message)) {
            firmwareVersion = message;
            mflight.notifyFlightEvent(FlightEventsType.FIRMWARE);
        }
	}

    public static boolean isCopter(int type){
        switch (type) {
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return true;

            default:
                return false;
        }
    }

    public static boolean isPlane(int type){
        return type == MAV_TYPE.MAV_TYPE_FIXED_WING;
    }

    public static boolean isRover(int type){
        return type == MAV_TYPE.MAV_TYPE_GROUND_ROVER;
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch(event){
            case DISCONNECTED:
                setType(DEFAULT_TYPE);
                break;
        }
    }
}