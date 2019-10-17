package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;

public class Navigation extends FlightVariable {

	private double nav_pitch;
	private double nav_roll;
	private double nav_bearing;

	public Navigation(Flight mflight) {
		super(mflight);
	}

	public void setNavPitchRollYaw(float nav_pitch, float nav_roll, short nav_bearing) {
        if(this.nav_pitch != nav_pitch || this.nav_roll != nav_roll || this.nav_bearing != nav_bearing) {
            this.nav_pitch = nav_pitch;
            this.nav_roll = nav_roll;
            this.nav_bearing = nav_bearing;
            mflight.notifyFlightEvent(FlightEventsType.NAVIGATION);
        }
	}

	public double getNavPitch() {
		return nav_pitch;
	}

	public double getNavRoll() {
		return nav_roll;
	}

	public double getNavBearing() {
		return nav_bearing;
	}

}
