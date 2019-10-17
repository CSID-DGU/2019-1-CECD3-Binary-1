package huins.ex.core.Flight.variables;

import huins.ex.com.MAVLink.common.msg_heartbeat;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.Handler;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.gcs.GCSHeartbeat;
import huins.ex.core.model.Flight;

public class HeartBeat extends FlightVariable implements OnFlightListener {

    private static final long CONNECTION_TIMEOUT = 5000; //ms
    private static final long HEARTBEAT_NORMAL_TIMEOUT = 5000; //ms
    private static final long HEARTBEAT_LOST_TIMEOUT = 15000; //ms
    private static final long HEARTBEAT_IMU_CALIBRATION_TIMEOUT = 35000; //ms

    public static final int INVALID_MAVLINK_VERSION = -1;

    public HeartbeatState heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
    private byte sysid = 1;
    private byte compid = 1;
    private final GCSHeartbeat gcsHeartbeat;

    /**
     * Stores the version of the mavlink protocol.
     */
    private byte mMavlinkVersion = INVALID_MAVLINK_VERSION;

    public enum HeartbeatState {
        FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT, IMU_CALIBRATION
    }

    public final Handler watchdog;
    public final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onHeartbeatTimeout();
        }
    };

    public HeartBeat(Flight mflight, Handler handler) {
        super(mflight);
        this.watchdog = handler;
        this.gcsHeartbeat = new GCSHeartbeat(mflight, 1);
        mflight.addFlightListener(this);
    }

    public byte getSysid() {
        return sysid;
    }

    public byte getCompid() {
        return compid;
    }

    /**
     * @return the version of the mavlink protocol.
     */
    public byte getMavlinkVersion() {
        return mMavlinkVersion;
    }

    public void onHeartbeat(msg_heartbeat msg) {
        sysid = (byte) msg.sysid;
        compid = (byte) msg.compid;
        mMavlinkVersion = msg.mavlink_version;

        switch (heartbeatState) {
            case FIRST_HEARTBEAT:
                notifyConnected();
                System.out.println("Received first heartbeat.");
                mflight.notifyFlightEvent(FlightEventsType.HEARTBEAT_FIRST);
                break;

            case LOST_HEARTBEAT:
                mflight.notifyFlightEvent(FlightEventsType.HEARTBEAT_RESTORED);
                break;
        }

        heartbeatState = HeartbeatState.NORMAL_HEARTBEAT;
        restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
    }

    public boolean hasHeartbeat() {
        return heartbeatState != HeartbeatState.FIRST_HEARTBEAT;
    }

    public boolean isConnectionAlive() {
        return heartbeatState != HeartbeatState.LOST_HEARTBEAT;
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch (event) {
            case CALIBRATION_IMU:
                //Set the heartbeat in imu calibration mode.
                heartbeatState = HeartbeatState.IMU_CALIBRATION;
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                break;

            case CHECKING_VEHICLE_LINK:
                System.out.println("Received connecting event.");
                gcsHeartbeat.setActive(true);
                notifyConnecting();
                break;

            case CONNECTION_FAILED:
            case DISCONNECTED:
                gcsHeartbeat.setActive(false);
                notifyDisconnected();
                break;

            default:
                break;
        }
    }

    private void notifyConnecting(){
        restartWatchdog(CONNECTION_TIMEOUT);
    }

    private void notifyConnected() {
        restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
    }

    private void notifyDisconnected() {
        watchdog.removeCallbacks(watchdogCallback);
        heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
        mMavlinkVersion = INVALID_MAVLINK_VERSION;
    }

    private void onHeartbeatTimeout() {
        switch (heartbeatState) {
            case IMU_CALIBRATION:
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                mflight.notifyFlightEvent(FlightEventsType.CALIBRATION_TIMEOUT);
                break;

            case FIRST_HEARTBEAT:
                System.out.println("First heartbeat timeout.");
                mflight.notifyFlightEvent(FlightEventsType.CONNECTION_FAILED);
                break;

            default:
                heartbeatState = HeartbeatState.LOST_HEARTBEAT;
                restartWatchdog(HEARTBEAT_LOST_TIMEOUT);
                mflight.notifyFlightEvent(FlightEventsType.HEARTBEAT_TIMEOUT);
                break;
        }
    }

    private void restartWatchdog(long timeout) {
        // re-start watchdog
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, timeout);
    }
}
