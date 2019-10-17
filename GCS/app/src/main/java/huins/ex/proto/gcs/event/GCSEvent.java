package huins.ex.proto.gcs.event;

/**
 * Stores the list of gcs events (as action), and their extra parameters.
 * The defined events are used in system broadcasts.
 */
public class GCSEvent {
    public static final String EXTRA_APP_ID = "extra.APP_ID";

    public static final String ACTION_VEHICLE_CONNECTION = "action.VEHICLE_CONNECTION";

    public static final String EXTRA_VEHICLE_CONNECTION_PARAMETER = "extra.VEHICLE_CONNECTION_PARAMETER";

    public static final String ACTION_VEHICLE_DISCONNECTION = "action.VEHICLE_DISCONNECTION";

    //Not instantiable
    private GCSEvent(){}
}
