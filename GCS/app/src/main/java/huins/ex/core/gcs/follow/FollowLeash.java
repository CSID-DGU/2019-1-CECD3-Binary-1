package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.geoTools.GeoTools;
import huins.ex.core.model.Flight;

public class FollowLeash extends FollowWithRadiusAlgorithm {

    public FollowLeash(Flight flight, FlightInterfaces.Handler handler, double radius) {
        super(flight, handler, radius);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.LEASH;
    }

    @Override
    protected void processNewLocation(Location location) {
        final Coord2D locationCoord = location.getCoord();
        final Coord2D dronePosition = flight.getGps().getPosition();

        if (locationCoord == null || dronePosition == null) {
            return;
        }

        if (GeoTools.getDistance(locationCoord, dronePosition) > radius) {
            double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(locationCoord, dronePosition);
            Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(locationCoord, headingGCStoDrone, radius);
            flight.getGuidedPoint().newGuidedCoord(goCoord);
        }
    }

}
