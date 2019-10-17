package huins.ex.proto.action;


/**
 * Contains builder methods use to generate connect or disconnect actions.
 */
public class StreamActions {

    public static final String ACTION_RECORD_START = "action.RECORD_START";
    public static final String ACTION_RECORD_STOP = "action.RECORD_STOP";

    public static final String ACTION_STREAM_CAPTURE = "action.STREAM_CAPTURE";
    public static final String ACTION_STREAM_ONOFF = "action.STREAM_ONOFF";

    public static final String ACTION_CAMERA_ON = "action.CAMERA_ON";
    public static final String ACTION_CAMERA_OFF = "action.CAMERA_OFF";

    public static final String ACTION_SET_RESOLUTION = "action.SET_RESOLUTION";

    public static final String EXTRA_CAMERA_DATA = "extra_camera_data";
    public static final String EXTRA_CAMERA_RESOLUTION = "extra_camera_resolution";
}

