package huins.ex.proto.gcs.action;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class CalibrationActions {

    public static final String ACTION_START_IMU_CALIBRATION = "action.START_IMU_CALIBRATION";
    public static final String ACTION_SEND_IMU_CALIBRATION_ACK = "SEND_IMU_CALIBRATION_ACK";

    public static final String EXTRA_IMU_STEP = "extra_step";

    public static final String ACTION_START_MAGNETOMETER_CALIBRATION = "START_MAGNETOMETER_CALIBRATION";
    public static final String ACTION_ACCEPT_MAGNETOMETER_CALIBRATION = "ACCEPT_MAGNETOMETER_CALIBRATION";
    public static final String ACTION_CANCEL_MAGNETOMETER_CALIBRATION = "CANCEL_MAGNETOMETER_CALIBRATION";

    public static final String EXTRA_RETRY_ON_FAILURE = "extra_retry_on_failure";
    public static final String EXTRA_SAVE_AUTOMATICALLY = "extra_save_automatically";
    public static final String EXTRA_START_DELAY = "extra_start_delay";

}
