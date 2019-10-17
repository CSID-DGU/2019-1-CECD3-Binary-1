package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.geoTools.GeoTools;
import huins.ex.core.helpers.math.MathUtil;
import huins.ex.core.model.Flight;

public class FollowCircle extends FollowWithRadiusAlgorithm {

    /**
     * °/s
     */
    private double circleStep = 2;
    private double circleAngle = 0.0;

    public FollowCircle(Flight flight, FlightInterfaces.Handler handler, double radius, double rate) {
        super(flight, handler, radius);
        circleStep = rate;
    }

    @Override
    public FollowModes getType() {
        return FollowModes.CIRCLE;
    }

    @Override
    public void processNewLocation(Location location) {
        Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
        Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, circleAngle, radius);
        circleAngle = MathUtil.constrainAngle(circleAngle + circleStep);
        flight.getGuidedPoint().newGuidedCoord(goCoord);
    }
}
