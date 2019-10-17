package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.geoTools.GeoTools;
import huins.ex.core.model.Flight;

public abstract class FollowHeadingAngle extends FollowWithRadiusAlgorithm {

    protected double angleOffset;

    protected FollowHeadingAngle(Flight flight, FlightInterfaces.Handler handler, double radius, double angleOffset) {
        super(flight, handler, radius);
        this.angleOffset = angleOffset;
    }

    @Override
    public void processNewLocation(Location location) {
        Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
        double bearing = location.getBearing();

        Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset, radius);
        flight.getGuidedPoint().newGuidedCoord(goCoord);
    }

}