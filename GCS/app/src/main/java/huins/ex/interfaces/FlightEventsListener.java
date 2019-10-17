package huins.ex.interfaces;

import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.variables.calibration.MagnetometerCalibrationImpl;
import huins.ex.proto.connection.FlightSharePreferences;

/**
 * Created by suhak on 15. 7. 15.
 */
public interface FlightEventsListener extends FlightInterfaces.OnFlightListener,
        FlightInterfaces.OnParameterManagerListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

    FlightSharePreferences getFlightSharePreferences();

    void onConnectionFailed(String error);

    void onReceivedMavLinkMessage(MAVLinkMessage msg);

    void onMessageLogged(int logLevel, String message);
}
