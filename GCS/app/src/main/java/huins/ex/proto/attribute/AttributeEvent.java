package huins.ex.proto.attribute;

/**
 * Stores all possible drone events.
 */
public class AttributeEvent {
 
    public static final String ATTITUDE_UPDATED = "ATTITUDE_UPDATED";


    public static final String AUTOPILOT_ERROR = "AUTOPILOT_ERROR";


    public static final String AUTOPILOT_MESSAGE = "AUTOPILOT_MESSAGE";

    public static final String CALIBRATION_MAG_STARTED = "CALIBRATION_MAG_STARTED";

    public static final String CALIBRATION_MAG_ESTIMATION = "CALIBRATION_MAG_ESTIMATION";

    public static final String CALIBRATION_MAG_CANCELLED = "CALIBRATION_MAG_CANCELLED";

    public static final String CALIBRATION_MAG_COMPLETED = "CALIBRATION_MAG_COMPLETED";

    public static final String CALIBRATION_MAG_PROGRESS = "CALIBRATION_MAG_PROGRESS";

    public static final String CALIBRATION_IMU = "CALIBRATION_IMU";
    public static final String CALIBRATION_IMU_ERROR = "CALIBRATION_IMU_ERROR";
    public static final String CALIBRATION_IMU_TIMEOUT = "CALIBRATION_IMU_TIMEOUT";

    public static final String FOLLOW_START = "FOLLOW_START";
    public static final String FOLLOW_STOP = "FOLLOW_STOP";
    public static final String FOLLOW_UPDATE = "FOLLOW_UPDATE";


    public static final String CAMERA_UPDATED = "CAMERA_UPDATED";
    public static final String CAMERA_FOOTPRINTS_UPDATED = "CAMERA_FOOTPRINTS_UPDATED";

    public static final String GUIDED_POINT_UPDATED = "GUIDED_POINT_UPDATED";

    public static final String MISSION_UPDATED = "MISSION_UPDATED";
    public static final String MISSION_DRONIE_CREATED = "MISSION_DRONIE_CREATED";
    public static final String MISSION_SENT = "MISSION_SENT";
    public static final String MISSION_RECEIVED = "MISSION_RECEIVED";
    public static final String MISSION_ITEM_UPDATED = "MISSION_ITEM_UPDATED";


    public static final String PARAMETERS_REFRESH_STARTED = "PARAMETERS_REFRESH_STARTED";

    public static final String PARAMETERS_REFRESH_COMPLETED = "PARAMETERS_REFRESH_ENDED";


    public static final String PARAMETER_RECEIVED = "PARAMETERS_RECEIVED";

    public static final String TYPE_UPDATED = "TYPE_UPDATED";

    public static final String SIGNAL_UPDATED = "SIGNAL_UPDATED";
    public static final String SIGNAL_WEAK = "SIGNAL_WEAK";

    public static final String SPEED_UPDATED = "SPEED_UPDATED";

    public static final String BATTERY_UPDATED = "BATTERY_UPDATED";

    public static final String STATE_UPDATED = "STATE_UPDATED";

    public static final String STATE_ARMING = "STATE_ARMING";
    public static final String STATE_CONNECTING = "STATE_CONNECTING";
    public static final String STATE_CONNECTED = "STATE_CONNECTED";
    public static final String STATE_DISCONNECTED = "STATE_DISCONNECTED";

    public static final String STATE_EKF_REPORT = "STATE_EKF_REPORT";


    public static final String STATE_EKF_POSITION = "STATE_EKF_POSITION";

    public static final String STATE_VEHICLE_MODE = "STATE_VEHICLE_MODE";

    public static final String HOME_UPDATED = "HOME_UPDATED";

    public static final String GPS_POSITION = "GPS_POSITION";
    public static final String GPS_FIX = "GPS_FIX";
    public static final String GPS_COUNT = "GPS_COUNT";
    public static final String WARNING_NO_GPS = "WARNING_NO_GPS";

    public static final String HEARTBEAT_FIRST = "HEARTBEAT_FIRST";
    public static final String HEARTBEAT_RESTORED = "HEARTBEAT_RESTORED";
    public static final String HEARTBEAT_TIMEOUT = "HEARTBEAT_TIMEOUT";

    public static final String ALTITUDE_UPDATED = "ALTITUDE_UPDATED";


    public static final String GOPRO_STATE_UPDATED = "GOPRO_STATE_UPDATED";

    public static final String RTSP_STREAM_START = "RTSP_STREAM_START";
    public static final String RTSP_STREAM_STOP = "RTSP_STREAM_STOP";

    public static final String VIDEO_EO_START = "VIDEO_EO_START";
    public static final String VIDEO_IR_START = "VIDEO_IR_START";

    public static final String RTSP_STREAM_ONOFF = "RTSP_STREAM_ONOFF";
    public static final String RTSP_STREAM_CAPTURE = "RTSP_STREAM_CAPTURE";

    public static final String DRONE_CAMERA_ON = "DRONE_CAMERA_ON";
    public static final String DRONE_CAMERA_OFF = "DRONE_CAMERA_OFF";
}
