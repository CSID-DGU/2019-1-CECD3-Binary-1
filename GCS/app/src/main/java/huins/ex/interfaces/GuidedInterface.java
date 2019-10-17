package huins.ex.interfaces;

import android.os.Bundle;

import huins.ex.proto.Coordinate;
import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Gps;

import static huins.ex.proto.action.GuidedActions.ACTION_DO_GUIDED_TAKEOFF;
import static huins.ex.proto.action.GuidedActions.ACTION_SEND_GUIDED_POINT;
import static huins.ex.proto.action.GuidedActions.ACTION_SET_GUIDED_ALTITUDE;
import static huins.ex.proto.action.GuidedActions.EXTRA_ALTITUDE;
import static huins.ex.proto.action.GuidedActions.EXTRA_FORCE_GUIDED_POINT;
import static huins.ex.proto.action.GuidedActions.EXTRA_GUIDED_POINT;

/**
 * Created by suhak on 15. 7. 16.
 */

public class GuidedInterface {
    
    public static void takeoff(Flight flight, double altitude) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        flight.performAsyncAction(new Action(ACTION_DO_GUIDED_TAKEOFF, params));
    }

    /**
     * Send a guided point to the connected flight.
     *
     * @param point guided point location
     * @param force true to enable guided mode is required.
     */
    public static void sendGuidedPoint(Flight flight, Coordinate point, boolean force) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_GUIDED_POINT, force);
        params.putParcelable(EXTRA_GUIDED_POINT, point);
        flight.performAsyncAction(new Action(ACTION_SEND_GUIDED_POINT, params));
    }

    /**
     * Set the altitude for the guided point.
     *
     * @param altitude altitude in meters
     */
    public static void setGuidedAltitude(Flight flight, double altitude) {
        Bundle params = new Bundle();
        params.putDouble(EXTRA_ALTITUDE, altitude);
        flight.performAsyncAction(new Action(ACTION_SET_GUIDED_ALTITUDE, params));
    }

    public static void pauseAtCurrentLocation(final Flight flight){
        flight.getAttributeAsync(AttributeType.GPS, new Flight.AttributeRetrievedListener<Gps>() {
            @Override
            public void onRetrievalSucceed(Gps gps) {
                sendGuidedPoint(flight, gps.getPosition(), true);
            }
        });
    }
}
