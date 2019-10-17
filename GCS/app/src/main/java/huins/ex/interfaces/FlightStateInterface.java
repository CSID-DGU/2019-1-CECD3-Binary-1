package huins.ex.interfaces;

import android.os.Bundle;

import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;
import huins.ex.proto.property.VehicleMode;

import static huins.ex.proto.action.StateActions.ACTION_ARM;
import static huins.ex.proto.action.StateActions.ACTION_SET_VEHICLE_MODE;
import static huins.ex.proto.action.StateActions.EXTRA_ARM;
import static huins.ex.proto.action.StateActions.EXTRA_VEHICLE_MODE;

/**
 * Created by suhak on 15. 7. 16.
 */
public class FlightStateInterface {

    /**
     * Arm or disarm the connected drone.
     *
     * @param arm true to arm, false to disarm.
     */
    public static void arm(Flight flight, boolean arm) {
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_ARM, arm);
        flight.performAsyncAction(new Action(ACTION_ARM, params));
    }

    /**
     * Change the vehicle mode for the connected drone.
     *
     * @param newMode new vehicle mode.
     */
    public static void setVehicleMode(Flight flight, VehicleMode newMode) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_VEHICLE_MODE, newMode);
        flight.performAsyncAction(new Action(ACTION_SET_VEHICLE_MODE, params));
    }
}
