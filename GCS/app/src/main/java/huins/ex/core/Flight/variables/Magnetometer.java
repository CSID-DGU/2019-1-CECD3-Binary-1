package huins.ex.core.Flight.variables;

import huins.ex.com.MAVLink.common.msg_raw_imu;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;

public class Magnetometer extends FlightVariable {

	private int x;
	private int y;
	private int z;

	public Magnetometer(Flight mflight) {
		super(mflight);
	}

	public void newData(msg_raw_imu msg_imu) {
		x = msg_imu.xmag;
		y = msg_imu.ymag;
		z = msg_imu.zmag;
		mflight.notifyFlightEvent(FlightEventsType.MAGNETOMETER);
	}

	public int[] getVector() {
		return new int[] { x, y, z };
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int[] getOffsets() {
		Parameter paramX = mflight.getParameters().getParameter("COMPASS_OFS_X");
		Parameter paramY = mflight.getParameters().getParameter("COMPASS_OFS_Y");
		Parameter paramZ = mflight.getParameters().getParameter("COMPASS_OFS_Z");
		if (paramX == null || paramY == null || paramZ == null) {
			return null;
		}
		return new int[]{(int) paramX.value,(int) paramY.value,(int) paramZ.value};

	}
}
