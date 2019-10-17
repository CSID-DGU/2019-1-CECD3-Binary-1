package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.model.Flight;

public class FollowLead extends FollowHeadingAngle {

    public FollowLead(Flight flight, FlightInterfaces.Handler handler, double radius) {
        super(flight, handler, radius, 0.0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEAD;
    }

}
