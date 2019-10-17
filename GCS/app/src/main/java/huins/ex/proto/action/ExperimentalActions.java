package huins.ex.proto.action;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class ExperimentalActions {

    public static final String ACTION_TRIGGER_CAMERA = "action.TRIGGER_CAMERA";

    public static final String ACTION_EPM_COMMAND = "action.EPM_COMMAND";
    public static final String EXTRA_EPM_RELEASE = "extra_epm_release";

    public static final String ACTION_SEND_MAVLINK_MESSAGE = "action.SEND_MAVLINK_MESSAGE";
    public static final String EXTRA_MAVLINK_MESSAGE = "extra_mavlink_message";

    public static final String ACTION_SET_RELAY = "action.SET_RELAY";
    public static final String EXTRA_RELAY_NUMBER = "extra_relay_number";
    public static final String EXTRA_IS_RELAY_ON = "extra_is_relay_on";

    public static final String ACTION_SET_SERVO = "action.SET_SERVO";
    public static final String EXTRA_SERVO_CHANNEL = "extra_servo_channel";
    public static final String EXTRA_SERVO_PWM = "extra_servo_PWM";

}
