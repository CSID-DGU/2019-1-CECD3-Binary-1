package huins.ex.core.Flight.camera;


import java.util.HashMap;

import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_get_request;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_get_response;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_heartbeat;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_set_request;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_set_response;
import huins.ex.com.MAVLink.enums.GOPRO_COMMAND;
import huins.ex.com.MAVLink.enums.GOPRO_HEARTBEAT_STATUS;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.Handler;
import huins.ex.core.model.Flight;

/**
 * Created by Fredia Huya-Kouadio on 4/7/15.
 */
public class GoProImpl implements FlightInterfaces.OnFlightListener {

    private static final long HEARTBEAT_TIMEOUT = 5000l; //ms

    private int status = GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_DISCONNECTED;
    private boolean isRecording;

    private final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onHeartbeatTimeout();
        }
    };

    private final HashMap<Integer, GetResponseHandler> getResponsesFutures = new HashMap<>();
    private final HashMap<Integer, SetResponseHandler> setResponsesFutures = new HashMap<>();

    private final msg_gopro_get_request scratchGetRequest;
    private final msg_gopro_set_request scratchSetRequest;

    private final Flight flight;
    private final Handler watchdog;

    public GoProImpl(Flight flight, Handler handler) {
        this.flight = flight;
        this.watchdog = handler;

        this.scratchGetRequest = new msg_gopro_get_request();
        this.scratchGetRequest.sysid = 255;
        this.scratchGetRequest.compid = 0;
        this.scratchGetRequest.target_system = 0;

        this.scratchSetRequest = new msg_gopro_set_request();
        this.scratchSetRequest.sysid = 255;
        this.scratchSetRequest.compid = 0;
        this.scratchSetRequest.target_system = 0;

        if (flight.isConnected()) {
            updateRequestTarget();
        }
    }

    public void onHeartBeat(msg_gopro_heartbeat heartBeat) {
        this.scratchSetRequest.target_component = (byte) heartBeat.compid;
        this.scratchGetRequest.target_component = (byte) heartBeat.compid;

        if (status != heartBeat.status) {
            status = heartBeat.status;
            flight.notifyFlightEvent(FlightEventsType.GOPRO_STATUS_UPDATE);

            if (!isConnected()) {
                resetFutures();
            }
        }

        restartWatchdog();
    }

    /**
     * Handles responses from gopro set requests.
     *
     * @param response
     */
    public void onResponseReceived(msg_gopro_set_response response) {
        if (response == null)
            return;

        final SetResponseHandler responseHandler = setResponsesFutures.remove((int) response.cmd_id);
        if (responseHandler != null) {
            responseHandler.onResponse(response.cmd_id, response.result == 1);
        }
    }

    /**
     * Handles responses from gopro get requests.
     *
     * @param response
     */
    public void onResponseReceived(msg_gopro_get_response response) {
        if (response == null)
            return;

        final GetResponseHandler responseHandler = getResponsesFutures.remove((int) response.cmd_id);
        if (responseHandler != null) {
            responseHandler.onResponse(response.cmd_id, response.value);
        }
    }

    private void onHeartbeatTimeout() {
        if (status == GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_DISCONNECTED)
            return;

        status = GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_DISCONNECTED;
        resetFutures();

        flight.notifyFlightEvent(FlightEventsType.GOPRO_STATUS_UPDATE);
    }

    private void restartWatchdog() {
        //re-start watchdog
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, HEARTBEAT_TIMEOUT);
    }

    public boolean isConnected() {
        return status == GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_CONNECTED
                || status == GOPRO_HEARTBEAT_STATUS.GOPRO_HEARTBEAT_STATUS_RECORDING;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void startRecording() {
        if (!isConnected() || isRecording())
            return;

        //Start recording
        sendSetRequest(GOPRO_COMMAND.GOPRO_COMMAND_SHUTTER, 1, new SetResponseHandler() {
            @Override
            public void onResponse(byte commandId, boolean success) {
                if(success != isRecording) {
                    isRecording = success;
                    flight.notifyFlightEvent(FlightEventsType.GOPRO_STATUS_UPDATE);
                }
            }
        });
    }

    public void stopRecording() {
        if (!isConnected() || !isRecording())
            return;

        //Stop recording
        sendSetRequest(GOPRO_COMMAND.GOPRO_COMMAND_SHUTTER, 0, new SetResponseHandler() {
            @Override
            public void onResponse(byte commandId, boolean success) {
                if (success == isRecording) {
                    isRecording = !success;
                    flight.notifyFlightEvent(FlightEventsType.GOPRO_STATUS_UPDATE);
                }
            }
        });
    }

    private void sendSetRequest(int commandId, int value, SetResponseHandler future) {
        if (future != null)
            setResponsesFutures.put(commandId, future);
        scratchSetRequest.cmd_id = (byte) commandId;
        scratchSetRequest.value = (byte) value;
        sendMavlinkPacket(scratchSetRequest.pack());
    }

    private void sendGetRequest(int commandId, GetResponseHandler future) {
        if (future != null)
            getResponsesFutures.put(commandId, future);

        scratchGetRequest.cmd_id = (byte) commandId;
        sendMavlinkPacket(scratchGetRequest.pack());
    }

    private void sendMavlinkPacket(MAVLinkPacket packet) {
        packet.sysid = 255;
        packet.compid = 0;
        flight.getMavClient().sendMavPacket(packet);
    }

    private void resetFutures() {
        getResponsesFutures.clear();
        setResponsesFutures.clear();
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case HEARTBEAT_RESTORED:
                updateRequestTarget();
                break;
        }
    }

    private void updateRequestTarget() {
        scratchGetRequest.target_component = scratchSetRequest.target_component = flight.getCompid();
        scratchGetRequest.target_system = scratchSetRequest.target_system = flight.getSysid();
    }

    /**
     * Callable used to handle the response from a request.
     */
    private static abstract class GetResponseHandler {

        public abstract void onResponse(byte commandId, byte value);
    }

    private static abstract class SetResponseHandler {
        public abstract void onResponse(byte commandId, boolean success);
    }
}
