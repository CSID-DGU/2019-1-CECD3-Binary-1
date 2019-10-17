package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;

public class MissionStats extends FlightVariable {
    private double distanceToWp = 0;
    private short currentWP = -1;

    public MissionStats(Flight mflight) {
        super(mflight);
    }

    public void setDistanceToWp(double disttowp) {
        this.distanceToWp = disttowp;
    }

    public void setWpno(short seq) {
        if (seq != currentWP) {
            this.currentWP = seq;
            mflight.notifyFlightEvent(FlightEventsType.MISSION_WP_UPDATE);
        }
    }

    public int getCurrentWP() {
        return currentWP;
    }

    public double getDistanceToWP() {
        return distanceToWp;
    }

}
