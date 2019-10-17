package huins.ex.interfaces;

import android.os.Bundle;

import huins.ex.proto.connection.ConnectionResult;

/**
 * Created by suhak on 15. 7. 14.
 */
public interface FlightListener {
    void onFlightConnectionFailed(ConnectionResult result);

    void onFlightEvent(String event, Bundle extras);

    void onFlightServiceInterrupted(String errorMsg);
}
