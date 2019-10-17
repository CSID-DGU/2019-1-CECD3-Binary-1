package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.model.Flight;

public class FollowRight extends FollowHeadingAngle {

    public FollowRight(Flight flight, FlightInterfaces.Handler handler, double radius) {
        super(flight, handler, radius, 90.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.RIGHT;
    }

}
