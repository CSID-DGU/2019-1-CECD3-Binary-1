package huins.ex.model;

/**
 * Created by suhak on 15. 6. 26.
 */
public class FlightEvent {
    private static final String GCS_PREFIX = "huins.GCS.event";

    public static final String ATTITUDE_UPDATED = GCS_PREFIX + "ATTITUDE_UPDATED";

    public static final String AUTOPILOT_ERROR = GCS_PREFIX + "AUTOPILOT_ERROR";

    public static final String AUTOPILOT_MESSAGE = GCS_PREFIX + "AUTOPILOT_MESSAGE";

    public static final String CALIBRATION_MAG_CANCELLED = GCS_PREFIX + "CALIBRATION_MAG_CANCELLED";

    public static final String CALIBRATION_MAG_COMPLETED = GCS_PREFIX + "CALIBRATION_MAG_COMPLETED";

    public static final String CALIBRATION_MAG_PROGRESS = GCS_PREFIX + "CALIBRATION_MAG_PROGRESS";

    public static final String CALIBRATION_IMU = GCS_PREFIX + "CALIBRATION_IMU";
    public static final String CALIBRATION_IMU_ERROR = GCS_PREFIX + "CALIBRATION_IMU_ERROR";
    public static final String CALIBRATION_IMU_TIMEOUT = GCS_PREFIX + "CALIBRATION_IMU_TIMEOUT";

    public static final String FOLLOW_START = GCS_PREFIX + "FOLLOW_START";
    public static final String FOLLOW_STOP = GCS_PREFIX + "FOLLOW_STOP";
    public static final String FOLLOW_UPDATE = GCS_PREFIX + "FOLLOW_UPDATE";
    
    public static final String CAMERA_UPDATED = GCS_PREFIX + "CAMERA_UPDATED";
    public static final String CAMERA_FOOTPRINTS_UPDATED = GCS_PREFIX + "CAMERA_FOOTPRINTS_UPDATED";

    public static final String GUIDED_POINT_UPDATED = GCS_PREFIX + "GUIDED_POINT_UPDATED";

    public static final String MISSION_UPDATED = GCS_PREFIX + "MISSION_UPDATED";
    public static final String MISSION_DRONIE_CREATED = GCS_PREFIX + "MISSION_DRONIE_CREATED";
    public static final String MISSION_SENT = GCS_PREFIX + "MISSION_SENT";
    public static final String MISSION_RECEIVED = GCS_PREFIX + "MISSION_RECEIVED";
    public static final String MISSION_ITEM_UPDATED = GCS_PREFIX + "MISSION_ITEM_UPDATED";

    public static final String PARAMETERS_REFRESH_STARTED = GCS_PREFIX + "PARAMETERS_REFRESH_STARTED";

    public static final String PARAMETERS_REFRESH_COMPLETED = GCS_PREFIX + "PARAMETERS_REFRESH_ENDED";

    public static final String PARAMETER_RECEIVED = GCS_PREFIX + "PARAMETERS_RECEIVED";

    public static final String TYPE_UPDATED = GCS_PREFIX + "TYPE_UPDATED";

    public static final String SIGNAL_UPDATED = GCS_PREFIX + "SIGNAL_UPDATED";
    public static final String SIGNAL_WEAK = GCS_PREFIX + "SIGNAL_WEAK";

    public static final String SPEED_UPDATED = GCS_PREFIX + "SPEED_UPDATED";

    public static final String BATTERY_UPDATED = GCS_PREFIX + "BATTERY_UPDATED";

    public static final String STATE_UPDATED = GCS_PREFIX + "STATE_UPDATED";

    public static final String STATE_ARMING = GCS_PREFIX + "STATE_ARMING";
    public static final String STATE_CONNECTING = GCS_PREFIX + "STATE_CONNECTING";
    public static final String STATE_CONNECTED = GCS_PREFIX + "STATE_CONNECTED";
    public static final String STATE_DISCONNECTED = GCS_PREFIX + "STATE_DISCONNECTED";

    public static final String STATE_EKF_REPORT = GCS_PREFIX + "STATE_EKF_REPORT";

    public static final String STATE_EKF_POSITION = GCS_PREFIX + "STATE_EKF_POSITION";
    
    public static final String STATE_VEHICLE_MODE = GCS_PREFIX + "STATE_VEHICLE_MODE";

    public static final String HOME_UPDATED = GCS_PREFIX + "HOME_UPDATED";

    public static final String GPS_POSITION = GCS_PREFIX + "GPS_POSITION";
    public static final String GPS_FIX = GCS_PREFIX + "GPS_FIX";
    public static final String GPS_COUNT = GCS_PREFIX + "GPS_COUNT";
    public static final String WARNING_NO_GPS = GCS_PREFIX + "WARNING_NO_GPS";

    public static final String HEARTBEAT_FIRST = GCS_PREFIX + "HEARTBEAT_FIRST";
    public static final String HEARTBEAT_RESTORED = GCS_PREFIX + "HEARTBEAT_RESTORED";
    public static final String HEARTBEAT_TIMEOUT = GCS_PREFIX + "HEARTBEAT_TIMEOUT";

    public static final String ALTITUDE_UPDATED = GCS_PREFIX + "ALTITUDE_UPDATED";

    public static final String GOPRO_STATE_UPDATED = GCS_PREFIX + "GOPRO_STATE_UPDATED";
}
