package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.model.Flight;

public class GPS extends FlightVariable {
	public final static int LOCK_2D = 2;
	public final static int LOCK_3D = 3;

	private double gps_eph = -1;
	private int satCount = -1;
	private int fixType = -1;
	private Coord2D position;

	public GPS(Flight mflight) {
		super(mflight);
	}

	public boolean isPositionValid() {
		return (position != null);
	}

	public Coord2D getPosition() {
		return position;
	}

	public double getGpsEPH() {
		return gps_eph;
	}

	public int getSatCount() {
		return satCount;
	}

	public String getFixType() {
		String gpsFix = "";
		switch (fixType) {
		case LOCK_2D:
			gpsFix = ("2D");
			break;
		case LOCK_3D:
			gpsFix = ("3D");
			break;
		default:
			gpsFix = ("NoFix");
			break;
		}
		return gpsFix;
	}

	public int getFixTypeNumeric() {
		return fixType;
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if (satCount != satellites_visible) {
			satCount = satellites_visible;
			gps_eph = (double) eph / 100; // convert from eph(cm) to gps_eph(m)
			mflight.notifyFlightEvent(FlightEventsType.GPS_COUNT);
		}
		if (fixType != fix) {
			fixType = fix;
			mflight.notifyFlightEvent(FlightEventsType.GPS_FIX);
		}
	}

	public void setPosition(double latitude, double longitude) {
        boolean positionUpdated = false;
        if(this.position == null){
            this.position = new Coord2D(latitude, longitude);
            positionUpdated = true;
        }
        else if(this.position.getLat() != latitude || this.position.getLng() != longitude){
            this.position.set(latitude, longitude);
            positionUpdated = true;
        }

		if (positionUpdated) {
			mflight.notifyFlightEvent(FlightEventsType.GPS);
		}
	}
}