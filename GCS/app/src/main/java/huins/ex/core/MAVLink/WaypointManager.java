package huins.ex.core.MAVLink;
import java.util.ArrayList;
import java.util.List;

import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.common.msg_mission_ack;
import huins.ex.com.MAVLink.common.msg_mission_count;
import huins.ex.com.MAVLink.common.msg_mission_current;
import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.com.MAVLink.common.msg_mission_item_reached;
import huins.ex.com.MAVLink.common.msg_mission_request;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightInterfaces.OnWaypointManagerListener;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;

/**
 * Class to manage the communication of waypoints to the MAV.
 * <p/>
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 */
public class WaypointManager extends FlightVariable {
    enum WaypointStates {
        IDLE, READ_REQUEST, READING_WP, WRITING_WP_COUNT, WRITING_WP, WAITING_WRITE_ACK
    }

    public enum WaypointEvent_Type {
        WP_UPLOAD, WP_DOWNLOAD, WP_RETRY, WP_CONTINUE, WP_TIMED_OUT
    }

    private static final long TIMEOUT = 15000; //ms
    private static final int RETRY_LIMIT = 3;

    private int retryTracker = 0;

    private int readIndex;
    private int writeIndex;
    private int retryIndex;
    private OnWaypointManagerListener wpEventListener;

    WaypointStates state = WaypointStates.IDLE;

    /**
     * waypoint witch is currently being written
     */

    private final FlightInterfaces.Handler watchdog;

    private final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            if (processTimeOut(++retryTracker))
                watchdog.postDelayed(this, TIMEOUT);
        }
    };

    public WaypointManager(Flight flight, FlightInterfaces.Handler handler) {
        super(flight);
        this.watchdog = handler;
    }

    public void setWaypointManagerListener(OnWaypointManagerListener wpEventListener) {
        this.wpEventListener = wpEventListener;
    }

    private void startWatchdog() {
        stopWatchdog();

        retryTracker = 0;
        this.watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }

    private void stopWatchdog() {
        this.watchdog.removeCallbacks(watchdogCallback);
    }

    /**
     * Try to receive all waypoints from the MAV.
     * <p/>
     * If all runs well the callback will return the list of waypoints.
     */
    public void getWaypoints() {
        // ensure that WPManager is not doing anything else
        if (state != WaypointStates.IDLE)
            return;

        doBeginWaypointEvent(WaypointEvent_Type.WP_DOWNLOAD);
        readIndex = -1;
        state = WaypointStates.READ_REQUEST;
        MavLinkWaypoint.requestWaypointsList(mflight);

        startWatchdog();
    }

    /**
     * Write a list of waypoints to the MAV.
     * <p/>
     * The callback will return the status of this operation
     *
     * @param data waypoints to be written
     */

    public void writeWaypoints(List<msg_mission_item> data) {
        // ensure that WPManager is not doing anything else
        if (state != WaypointStates.IDLE)
            return;

        if ((mission != null)) {
            doBeginWaypointEvent(WaypointEvent_Type.WP_UPLOAD);
            updateMsgIndexes(data);
            mission.clear();
            mission.addAll(data);
            writeIndex = 0;
            state = WaypointStates.WRITING_WP_COUNT;
            MavLinkWaypoint.sendWaypointCount(mflight, mission.size());

            startWatchdog();
        }
    }

    private void updateMsgIndexes(List<msg_mission_item> data) {
        short index = 0;
        for (msg_mission_item msg : data) {
            msg.seq = index++;
        }
    }

    /**
     * Sets the current waypoint in the MAV
     * <p/>
     * The callback will return the status of this operation
     */
    public void setCurrentWaypoint(int i) {
        if ((mission != null)) {
            MavLinkWaypoint.sendSetCurrentWaypoint(mflight, (short) i);
        }
    }

    /**
     * Callback for when a waypoint has been reached
     *
     * @param wpNumber number of the completed waypoint
     */
    public void onWaypointReached(int wpNumber) {
    }

    /**
     * Callback for a change in the current waypoint the MAV is heading for
     *
     * @param seq number of the updated waypoint
     */
    private void onCurrentWaypointUpdate(short seq) {
    }

    /**
     * number of waypoints to be received, used when reading waypoints
     */
    private short waypointCount;
    /**
     * list of waypoints used when writing or receiving
     */
    private List<msg_mission_item> mission = new ArrayList<msg_mission_item>();

    /**
     * Try to process a Mavlink message if it is a mission related message
     *
     * @param msg Mavlink message to process
     * @return Returns true if the message has been processed
     */
    public boolean processMessage(MAVLinkMessage msg) {
        switch (state) {
            default:
            case IDLE:
                break;

            case READ_REQUEST:
                if (msg.msgid == msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT) {
                    waypointCount = ((msg_mission_count) msg).count;
                    mission.clear();
                    startWatchdog();
                    MavLinkWaypoint.requestWayPoint(mflight, mission.size());
                    state = WaypointStates.READING_WP;
                    return true;
                }
                break;

            case READING_WP:
                if (msg.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM) {
                    startWatchdog();
                    processReceivedWaypoint((msg_mission_item) msg);
                    doWaypointEvent(WaypointEvent_Type.WP_DOWNLOAD, readIndex + 1, waypointCount);
                    if (mission.size() < waypointCount) {
                        MavLinkWaypoint.requestWayPoint(mflight, mission.size());
                    } else {
                        stopWatchdog();
                        state = WaypointStates.IDLE;
                        MavLinkWaypoint.sendAck(mflight);
                        mflight.getMission().onMissionReceived(mission);
                        doEndWaypointEvent(WaypointEvent_Type.WP_DOWNLOAD);
                    }
                    return true;
                }
                break;

            case WRITING_WP_COUNT:
                state = WaypointStates.WRITING_WP;
            case WRITING_WP:
                if (msg.msgid == msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST) {
                    startWatchdog();
                    processWaypointToSend((msg_mission_request) msg);
                    doWaypointEvent(WaypointEvent_Type.WP_UPLOAD, writeIndex + 1, mission.size());
                    return true;
                }
                break;

            case WAITING_WRITE_ACK:
                if (msg.msgid == msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK) {
                    stopWatchdog();
                    mflight.getMission().onWriteWaypoints((msg_mission_ack) msg);
                    state = WaypointStates.IDLE;
                    doEndWaypointEvent(WaypointEvent_Type.WP_UPLOAD);
                    return true;
                }
                break;
        }

        if (msg.msgid == msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED) {
            onWaypointReached(((msg_mission_item_reached) msg).seq);
            return true;
        }
        if (msg.msgid == msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT) {
            onCurrentWaypointUpdate(((msg_mission_current) msg).seq);
            return true;
        }
        return false;
    }

    public boolean processTimeOut(int mTimeOutCount) {

        // If max retry is reached, set state to IDLE. No more retry.
        if (mTimeOutCount >= RETRY_LIMIT) {
            state = WaypointStates.IDLE;
            doWaypointEvent(WaypointEvent_Type.WP_TIMED_OUT, retryIndex, RETRY_LIMIT);
            return false;
        }

        retryIndex++;
        doWaypointEvent(WaypointEvent_Type.WP_RETRY, retryIndex, RETRY_LIMIT);

        switch (state) {
            default:
            case IDLE:
                break;

            case READ_REQUEST:
                MavLinkWaypoint.requestWaypointsList(mflight);
                break;

            case READING_WP:
                if (mission.size() < waypointCount) { // request last lost WP
                    MavLinkWaypoint.requestWayPoint(mflight, mission.size());
                }
                break;

            case WRITING_WP_COUNT:
                MavLinkWaypoint.sendWaypointCount(mflight, mission.size());
                break;

            case WRITING_WP:
                // Log.d("TIMEOUT", "re Write Msg: " + String.valueOf(writeIndex));
                if (writeIndex < mission.size()) {
                    mflight.getMavClient().sendMavPacket(mission.get(writeIndex).pack());
                }
                break;

            case WAITING_WRITE_ACK:
                mflight.getMavClient().sendMavPacket(mission.get(mission.size() - 1).pack());
                break;
        }

        return true;
    }

    private void processWaypointToSend(msg_mission_request msg) {
        /*
         * Log.d("TIMEOUT", "Write Msg: " + String.valueOf(msg.seq));
		 */
        writeIndex = msg.seq;
        msg_mission_item item = mission.get(writeIndex);
        item.target_system = mflight.getSysid();
        item.target_component = mflight.getCompid();
        mflight.getMavClient().sendMavPacket(item.pack());

        if (writeIndex + 1 >= mission.size()) {
            state = WaypointStates.WAITING_WRITE_ACK;
        }
    }

    private void processReceivedWaypoint(msg_mission_item msg) {
		/*
		 * Log.d("TIMEOUT", "Read Last/Curr: " + String.valueOf(readIndex) + "/"
		 * + String.valueOf(msg.seq));
		 */
        // in case of we receive the same WP again after retry
        if (msg.seq <= readIndex)
            return;

        readIndex = msg.seq;

        mission.add(msg);
    }

    private void doBeginWaypointEvent(WaypointEvent_Type wpEvent) {
        retryIndex = 0;

        if (wpEventListener == null)
            return;

        wpEventListener.onBeginWaypointEvent(wpEvent);
    }

    private void doEndWaypointEvent(WaypointEvent_Type wpEvent) {
        if (retryIndex > 0)// if retry successful, notify that we now continue
            doWaypointEvent(WaypointEvent_Type.WP_CONTINUE, retryIndex, RETRY_LIMIT);

        retryIndex = 0;

        if (wpEventListener == null)
            return;

        wpEventListener.onEndWaypointEvent(wpEvent);
    }

    private void doWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
        retryIndex = 0;

        if (wpEventListener == null)
            return;

        wpEventListener.onWaypointEvent(wpEvent, index, count);
    }

}
