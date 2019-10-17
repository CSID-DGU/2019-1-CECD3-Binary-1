package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.model.Flight;

public class FollowAbove extends FollowAlgorithm {

    public FollowAbove(Flight flight, FlightInterfaces.Handler handler) {
        super(flight, handler);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.ABOVE;
    }

    @Override
    protected void processNewLocation(Location location) {
        Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
        flight.getGuidedPoint().newGuidedCoord(gcsCoord);
    }

}
