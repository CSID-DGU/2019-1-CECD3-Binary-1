package huins.ex.core.Flight.variables;

import huins.ex.com.MAVLink.Messages.ApmModes;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkModes;
import huins.ex.core.MAVLink.MavLinkTakeoff;
import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.model.Flight;

public class GuidedPoint extends FlightVariable implements OnFlightListener {

    private GuidedStates state = GuidedStates.UNINITIALIZED;
    private Coord2D coord = new Coord2D(0, 0);
    private double altitude = 0.0; //altitude in meters

    private Runnable mPostInitializationTask;

    public enum GuidedStates {
        UNINITIALIZED, IDLE, ACTIVE
    }

    public GuidedPoint(Flight mflight) {
        super(mflight);
        mflight.addFlightListener(this);
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case HEARTBEAT_RESTORED:
            case MODE:
                if (isGuidedMode(mflight)) {
                    initialize();
                } else {
                    disable();
                }
                break;

            case DISCONNECTED:
            case HEARTBEAT_TIMEOUT:
                disable();

            default:
                break;
        }
    }

    public static boolean isGuidedMode(Flight flight) {
        final int droneType = flight.getType();
        final ApmModes droneMode = flight.getState().getMode();

        if (Type.isCopter(droneType)) {
            return droneMode == ApmModes.ROTOR_GUIDED;
        }

        if (Type.isPlane(droneType)) {
            return droneMode == ApmModes.FIXED_WING_GUIDED;
        }

        if (Type.isRover(droneType)) {
            return droneMode == ApmModes.ROVER_GUIDED || droneMode == ApmModes.ROVER_HOLD;
        }

        return false;
    }

    public void pauseAtCurrentLocation() {
        if (state == GuidedStates.UNINITIALIZED) {
            changeToGuidedMode(mflight);
        } else {
            newGuidedCoord(mflight.getGps().getPosition());
            state = GuidedStates.IDLE;
        }
    }

    public static void changeToGuidedMode(Flight flight) {
        final State flightState = flight.getState();
        final int flightType = flight.getType();
        if (Type.isCopter(flightType)) {
            flightState.changeFlightMode(ApmModes.ROTOR_GUIDED);
        } else if (Type.isPlane(flightType)) {
            //You have to send a guided point to the plane in order to trigger guided mode.
            forceSendGuidedPoint(flight, flight.getGps().getPosition(), getDroneAltConstrained(flight));
        } else if (Type.isRover(flightType)) {
            flightState.changeFlightMode(ApmModes.ROVER_GUIDED);
        }
    }

    public void doGuidedTakeoff(double alt) {
        if (Type.isCopter(mflight.getType())) {
            coord = mflight.getGps().getPosition();
            altitude = alt;
            state = GuidedStates.IDLE;
            changeToGuidedMode(mflight);
            MavLinkTakeoff.sendTakeoff(mflight, alt);
            mflight.notifyFlightEvent(FlightEventsType.GUIDEDPOINT);
        }
    }

    public void newGuidedCoord(Coord2D coord) {
        changeCoord(coord);
    }

    public void newGuidedPosition(double latitude, double longitude, double altitude) {
        MavLinkModes.sendGuidedPosition(mflight, latitude, longitude, altitude);
    }

    public void newGuidedVelocity(double xVel, double yVel, double zVel) {
        MavLinkModes.sendGuidedVelocity(mflight, xVel, yVel, zVel);
    }

    public void newGuidedCoordAndVelocity(Coord2D coord, double xVel, double yVel, double zVel) {
        changeCoordAndVelocity(coord, xVel, yVel, zVel);
    }

    public void changeGuidedAltitude(double alt) {
        changeAlt(alt);
    }

    public void forcedGuidedCoordinate(final Coord2D coord) throws Exception {
        if ((mflight.getGps().getFixTypeNumeric() != GPS.LOCK_3D)) {
            throw new Exception("Bad GPS for guided");
        }

        if (isInitialized()) {
            changeCoord(coord);
        } else {
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                }
            };

            changeToGuidedMode(mflight);
        }
    }

    public void forcedGuidedCoordinate(final Coord2D coord, final double alt) throws Exception {
        if ((mflight.getGps().getFixTypeNumeric() != GPS.LOCK_3D)) {
            throw new Exception("Bad GPS for guided");
        }

        if (isInitialized()) {
            changeCoord(coord);
            changeAlt(alt);
        } else {
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                    changeAlt(alt);
                }
            };

            changeToGuidedMode(mflight);
        }
    }

    private void initialize() {
        if (state == GuidedStates.UNINITIALIZED) {
            coord = mflight.getGps().getPosition();
            altitude = getDroneAltConstrained(mflight);
            state = GuidedStates.IDLE;
            mflight.notifyFlightEvent(FlightEventsType.GUIDEDPOINT);
        }

        if (mPostInitializationTask != null) {
            mPostInitializationTask.run();
            mPostInitializationTask = null;
        }
    }

    private void disable() {
        if(state == GuidedStates.UNINITIALIZED)
            return;

        state = GuidedStates.UNINITIALIZED;
        mflight.notifyFlightEvent(FlightEventsType.GUIDEDPOINT);
    }

    private void changeAlt(double alt) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/

            case ACTIVE:
                altitude = alt;
                sendGuidedPoint();
                break;
        }
    }

    private void changeCoord(Coord2D coord) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/
            case ACTIVE:
                this.coord = coord;
                sendGuidedPoint();
                break;
        }
    }

    private void changeCoordAndVelocity(Coord2D coord, double xVel, double yVel, double zVel) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/
            case ACTIVE:
                this.coord = coord;
                sendGuidedPointAndVelocity(xVel, yVel, zVel);
                break;
        }
    }

    private void sendGuidedPointAndVelocity(double xVel, double yVel, double zVel) {
        if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPointAndVelocity(mflight, coord, altitude, xVel, yVel, zVel);
        }
    }

    private void sendGuidedPoint() {
        if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPoint(mflight, coord, altitude);
        }
    }

    public static void forceSendGuidedPoint(Flight flight, Coord2D coord, double altitudeInMeters) {
        flight.notifyFlightEvent(FlightEventsType.GUIDEDPOINT);
        if (coord != null) {
            MavLinkModes.setGuidedMode(flight, coord.getLat(), coord.getLng(), altitudeInMeters);
        }
    }

    public static void forceSendGuidedPointAndVelocity(Flight flight, Coord2D coord, double altitudeInMeters,
                                                       double xVel, double yVel, double zVel) {
        flight.notifyFlightEvent(FlightEventsType.GUIDEDPOINT);
        if (coord != null) {
            MavLinkModes.sendGuidedPositionAndVelocity(flight, coord.getLat(), coord.getLng(), altitudeInMeters, xVel,
                    yVel, zVel);
        }
    }

    private static double getDroneAltConstrained(Flight drone) {
        double alt = Math.floor(drone.getAltitude().getAltitude());
        return Math.max(alt, getDefaultMinAltitude(drone));
    }

    public Coord2D getCoord() {
        return coord;
    }

    public double getAltitude() {
        return this.altitude;
    }

    public boolean isActive() {
        return (state == GuidedStates.ACTIVE);
    }

    public boolean isIdle() {
        return (state == GuidedStates.IDLE);
    }

    public boolean isInitialized() {
        return !(state == GuidedStates.UNINITIALIZED);
    }

    public GuidedStates getState() {
        return state;
    }

    public static float getDefaultMinAltitude(Flight drone) {
        final int droneType = drone.getType();
        if (Type.isCopter(droneType)) {
            return 2f;
        } else if (Type.isPlane(droneType)) {
            return 15f;
        } else {
            return 0f;
        }
    }

}
