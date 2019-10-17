package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;

public class Altitude extends FlightVariable {
	private double altitude = 0;
	private double targetAltitude = 0;

	public Altitude(Flight mflight) {
		super(mflight);
	}

	public double getAltitude() {
		return altitude;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public void setAltitude(double altitude) {
        if(this.altitude != altitude) {
            this.altitude = altitude;
            mflight.notifyFlightEvent(FlightInterfaces.FlightEventsType.ALTITUDE);
        }
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

}