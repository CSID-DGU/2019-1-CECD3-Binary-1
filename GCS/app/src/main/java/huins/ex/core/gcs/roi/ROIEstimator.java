package huins.ex.core.gcs.roi;

import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.core.Flight.FlightInterfaces.Handler;
import huins.ex.core.MAVLink.command.doCmd.MavLinkDoCmds;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.gcs.location.Location.LocationReceiver;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.coordinates.Coord3D;
import huins.ex.core.helpers.geoTools.GeoTools;
import huins.ex.core.model.Flight;

/**
 * Uses location data from Android's FusedLocation LocationManager at 1Hz and
 * calculates new points at 10Hz based on Last Location and Last Velocity.
 */
public class ROIEstimator implements LocationReceiver {

    private static final int TIMEOUT = 100;
    protected Location realLocation;
    protected long timeOfLastLocation;

    protected final Flight flight;
    protected Handler watchdog;
    protected Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            updateROI();
        }

    };

    protected final AtomicBoolean isFollowEnabled = new AtomicBoolean(false);

    public ROIEstimator(Flight flight, Handler handler) {
        this.watchdog = handler;
        this.flight = flight;
    }

    public void enableFollow() {
        MavLinkDoCmds.resetROI(flight);
        isFollowEnabled.set(true);
    }

    public void disableFollow() {
        if (isFollowEnabled.compareAndSet(true, false)) {
            realLocation = null;
            MavLinkDoCmds.resetROI(flight);
            disableWatchdog();
        }
    }

    @Override
    public final void onLocationUpdate(Location location) {
        if (!isFollowEnabled.get())
            return;

        realLocation = location;
        timeOfLastLocation = System.currentTimeMillis();

        disableWatchdog();
        updateROI();
    }

    @Override
    public void onLocationUnavailable() {
        disableWatchdog();
    }

    protected void disableWatchdog() {
        watchdog.removeCallbacks(watchdogCallback);
    }

    protected void updateROI() {
        if (realLocation == null) {
            return;
        }

        Coord2D gcsCoord = realLocation.getCoord();

        double bearing = realLocation.getBearing();
        double distanceTraveledSinceLastPoint = realLocation.getSpeed()
                * (System.currentTimeMillis() - timeOfLastLocation) / 1000f;
        Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing, distanceTraveledSinceLastPoint);

        MavLinkDoCmds.setROI(flight, new Coord3D(goCoord.getLat(), goCoord.getLng(), (0.0)));

        if (realLocation.getSpeed() > 0)
            watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }

    public boolean isFollowEnabled() {
        return isFollowEnabled.get();
    }
}
