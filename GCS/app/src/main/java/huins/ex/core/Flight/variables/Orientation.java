package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;

public class Orientation extends FlightVariable {
	private double roll = 0;
	private double pitch = 0;
	private double yaw = 0;

	public Orientation(Flight mflight) {
		super(mflight);
	}

	public double getRoll() {
		return roll;
	}

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

	public void setRollPitchYaw(double roll, double pitch, double yaw) {
        if(this.roll != roll || this.pitch != pitch || this.yaw != yaw) {
            this.roll = roll;
            this.pitch = pitch;
            this.yaw = yaw;
            mflight.notifyFlightEvent(FlightEventsType.ATTITUDE);
        }
	}

}