package huins.ex.core.gcs.follow;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.variables.GuidedPoint;
import huins.ex.core.gcs.location.Location;
import huins.ex.core.gcs.roi.ROIEstimator;
import huins.ex.core.model.Flight;

public abstract class FollowAlgorithm {

    protected final Flight flight;
    private final ROIEstimator roiEstimator;
    private final AtomicBoolean isFollowEnabled = new AtomicBoolean(false);

    public FollowAlgorithm(Flight flight, FlightInterfaces.Handler handler) {
        this.flight = flight;
        this.roiEstimator = initROIEstimator(flight, handler);
    }

    protected boolean isFollowEnabled() {
        return isFollowEnabled.get();
    }

    public void enableFollow() {
        isFollowEnabled.set(true);
        roiEstimator.enableFollow();
    }

    public void disableFollow() {
        if(isFollowEnabled.compareAndSet(true, false)) {
            if (GuidedPoint.isGuidedMode(flight)) {
                flight.getGuidedPoint().pauseAtCurrentLocation();
            }

            roiEstimator.disableFollow();
        }
    }

    public void updateAlgorithmParams(Map<String, ?> paramsMap) {
    }

    protected ROIEstimator initROIEstimator(Flight flight, FlightInterfaces.Handler handler) {
        return new ROIEstimator(flight, handler);
    }

    protected ROIEstimator getROIEstimator() {
        return roiEstimator;
    }

    public final void onLocationReceived(Location location) {
        if (isFollowEnabled.get()) {
            roiEstimator.onLocationUpdate(location);
            processNewLocation(location);
        }
    }

    protected abstract void processNewLocation(Location location);

    public abstract FollowModes getType();

    public Map<String, Object> getParams() {
        return Collections.emptyMap();
    }

    public enum FollowModes {
        LEASH("Leash"),
        LEAD("Lead"),
        RIGHT("Right"),
        LEFT("Left"),
        CIRCLE("Orbit"),
        ABOVE("Above"),
        SPLINE_LEASH("Vector Leash"),
        SPLINE_ABOVE("Vector Above"),
        GUIDED_SCAN("Guided Scan"),
        LOOK_AT_ME("Look At Me");

        private String name;

        FollowModes(String str) {
            name = str;
        }

        @Override
        public String toString() {
            return name;
        }

        public FollowModes next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public FollowAlgorithm getAlgorithmType(Flight flight, FlightInterfaces.Handler handler) {
            switch (this) {
                case LEASH:
                default:
                    return new FollowLeash(flight, handler, 8.0);
                case LEAD:
                    return new FollowLead(flight, handler, 15.0);
                case RIGHT:
                    return new FollowRight(flight, handler, 10.0);
                case LEFT:
                    return new FollowLeft(flight, handler, 10.0);
                case CIRCLE:
                    return new FollowCircle(flight, handler, 15.0, 10.0);
                case ABOVE:
                    return new FollowAbove(flight, handler);
                case SPLINE_LEASH:
                    return new FollowSplineLeash(flight, handler, 8.0);
                case SPLINE_ABOVE:
                    return new FollowSplineAbove(flight, handler);
                case GUIDED_SCAN:
                    return new FollowGuidedScan(flight, handler);
                case LOOK_AT_ME:
                    return new FollowLookAtMe(flight, handler);
            }
        }
    }

}
