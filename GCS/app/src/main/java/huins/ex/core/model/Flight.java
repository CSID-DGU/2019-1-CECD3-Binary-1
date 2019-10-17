package huins.ex.core.model;

import huins.ex.com.MAVLink.common.msg_heartbeat;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.Preferences;
import huins.ex.core.Flight.camera.GoProImpl;
import huins.ex.core.Flight.profiles.Parameters;
import huins.ex.core.Flight.profiles.VehicleProfile;
import huins.ex.core.Flight.variables.Altitude;
import huins.ex.core.Flight.variables.Battery;
import huins.ex.core.Flight.variables.Camera;
import huins.ex.core.Flight.variables.GPS;
import huins.ex.core.Flight.variables.GuidedPoint;
import huins.ex.core.Flight.variables.Home;
import huins.ex.core.Flight.variables.Magnetometer;
import huins.ex.core.Flight.variables.MissionStats;
import huins.ex.core.Flight.variables.Navigation;
import huins.ex.core.Flight.variables.Orientation;
import huins.ex.core.Flight.variables.RC;
import huins.ex.core.Flight.variables.Radio;
import huins.ex.core.Flight.variables.Speed;
import huins.ex.core.Flight.variables.State;
import huins.ex.core.Flight.variables.StreamRates;
import huins.ex.core.Flight.variables.calibration.AccelCalibration;
import huins.ex.core.Flight.variables.calibration.MagnetometerCalibrationImpl;
import huins.ex.core.MAVLink.MAVLinkStreams;
import huins.ex.core.MAVLink.WaypointManager;
import huins.ex.core.firmware.FirmwareType;
import huins.ex.core.mission.Mission;

/**
 * Created by suhak on 15. 6. 26.
 */
public interface Flight {
    public void addFlightListener(FlightInterfaces.OnFlightListener listener);

    public void removeFlightListener(FlightInterfaces.OnFlightListener listener);

    public void notifyFlightEvent(FlightInterfaces.FlightEventsType event);

    public GPS getGps();

    public byte getSysid();

    public byte getCompid();

    public int getMavlinkVersion();

    public boolean isConnected();

    public boolean isConnectionAlive();

    public void onHeartbeat(msg_heartbeat msg_heart);

    public State getState();

    public Parameters getParameters();

    public void setType(int type);

    public int getType();

    public FirmwareType getFirmwareType();

    public void loadVehicleProfile();

    public VehicleProfile getVehicleProfile();

    public MAVLinkStreams.MAVLinkOutputStream getMavClient();

    public Preferences getPreferences();

    public WaypointManager getWaypointManager();

    public Speed getSpeed();

    public Battery getBattery();

    public Radio getRadio();

    public Home getHome();

    public Altitude getAltitude();

    public Orientation getOrientation();

    public Navigation getNavigation();

    public Mission getMission();

    public StreamRates getStreamRates();

    public MissionStats getMissionStats();

    public GuidedPoint getGuidedPoint();

    public AccelCalibration getCalibrationSetup();

    public MagnetometerCalibrationImpl getMagnetometerCalibration();

    public RC getRC();

    public Magnetometer getMagnetometer();

    public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb);

    public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error);

    public String getFirmwareVersion();

    public void setFirmwareVersion(String message);

    public Camera getCamera();

    public void logMessage(int mavSeverity, String message);

    /**
     * @return the GoPro instance for the connected vehicle.
     */
    public GoProImpl getGoProImpl();

}
