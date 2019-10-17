package huins.ex.core.gcs.follow;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.Handler;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.Flight.variables.GuidedPoint;
import huins.ex.core.Flight.variables.State;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.gcs.location.Location.LocationFinder;
import huins.ex.core.gcs.location.Location.LocationReceiver;
import huins.ex.core.model.Flight;

public class Follow implements OnFlightListener, LocationReceiver {

    private Location lastLocation;

    /**
     * Set of return value for the 'toggleFollowMeState' method.
     */
    public enum FollowStates {
        FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
    }

    private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
    private Flight flight;

    private LocationFinder locationFinder;
    private FollowAlgorithm followAlgorithm;

    public Follow(Flight flight, Handler handler, LocationFinder locationFinder) {
        this.flight = flight;
        flight.addFlightListener(this);

        followAlgorithm = FollowAlgorithm.FollowModes.LEASH.getAlgorithmType(flight, handler);

        this.locationFinder = locationFinder;
        locationFinder.setLocationListener(this);
    }

    public void toggleFollowMeState() {
        final State droneState = flight.getState();
        if (droneState == null) {
            state = FollowStates.FOLLOW_INVALID_STATE;
            return;
        }

        if (isEnabled()) {
            disableFollowMe();
        } else {
            if (flight.isConnected()) {
                if (droneState.isArmed()) {
                    GuidedPoint.changeToGuidedMode(flight);
                    enableFollowMe();
                } else {
                    state = FollowStates.FOLLOW_DRONE_NOT_ARMED;
                }
            } else {
                state = FollowStates.FOLLOW_DRONE_DISCONNECTED;
            }
        }
    }

    private void enableFollowMe() {
        lastLocation = null;
        state = FollowStates.FOLLOW_START;

        locationFinder.enableLocationUpdates();
        followAlgorithm.enableFollow();

        flight.notifyFlightEvent(FlightEventsType.FOLLOW_START);
    }

    private void disableFollowMe() {
        followAlgorithm.disableFollow();
        locationFinder.disableLocationUpdates();

        lastLocation = null;

        if (isEnabled()) {
            state = FollowStates.FOLLOW_END;
            flight.notifyFlightEvent(FlightEventsType.FOLLOW_STOP);
        }
    }

    public boolean isEnabled() {
        return state == FollowStates.FOLLOW_RUNNING || state == FollowStates.FOLLOW_START;
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch (event) {
            case MODE:
                if (isEnabled() && !GuidedPoint.isGuidedMode(flight)) {
                    disableFollowMe();
                }
                break;

            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                if(isEnabled()) {
                    disableFollowMe();
                }
                break;
        }
    }

    @Override
    public void onLocationUpdate(Location location) {
        if (location.isAccurate()) {
            state = FollowStates.FOLLOW_RUNNING;
            lastLocation = location;
            followAlgorithm.onLocationReceived(location);
        } else {
            state = FollowStates.FOLLOW_START;
        }

        flight.notifyFlightEvent(FlightEventsType.FOLLOW_UPDATE);
    }

    @Override
    public void onLocationUnavailable() {
        disableFollowMe();
    }

    public void setAlgorithm(FollowAlgorithm algorithm) {
        if(followAlgorithm != null && followAlgorithm != algorithm){
            followAlgorithm.disableFollow();
        }

        followAlgorithm = algorithm;
        if(isEnabled()){
            followAlgorithm.enableFollow();

            if(lastLocation != null)
                followAlgorithm.onLocationReceived(lastLocation);
        }
        flight.notifyFlightEvent(FlightEventsType.FOLLOW_CHANGE_TYPE);
    }

    public FollowAlgorithm getFollowAlgorithm() {
        return followAlgorithm;
    }

    public FollowStates getState() {
        return state;
    }
}
