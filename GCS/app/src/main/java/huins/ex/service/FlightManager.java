package huins.ex.service;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_report;
import huins.ex.com.MAVLink.enums.MAV_SEVERITY;
import huins.ex.communications.MAVLinkClient;
import huins.ex.core.Flight.FlightImpl;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.LogMessageListener;
import huins.ex.core.Flight.variables.calibration.MagnetometerCalibrationImpl;
import huins.ex.core.MAVLink.MAVLinkStreams;
import huins.ex.core.MAVLink.MavLinkMsgHandler;
import huins.ex.core.gcs.follow.Follow;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;
import huins.ex.interfaces.FlightEventsListener;
import huins.ex.proto.ConnectionParameter;
import huins.ex.proto.FusedLocation;
import huins.ex.proto.connection.FlightSharePreferences;
import huins.ex.util.AndroidApWarningParser;
import huins.ex.util.ConnectionException;
import huins.ex.util.GCSCorePreferences;
import huins.ex.util.analytics.GAUtils;
import huins.ex.view.fragments.helper.RcOutput;

/**
 * Created by suhak on 15. 6. 15.
 */
public class FlightManager implements MAVLinkStreams.MavlinkInputStream, FlightInterfaces.OnFlightListener,
        FlightInterfaces.OnParameterManagerListener, LogMessageListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

    private static final String TAG = FlightManager.class.getSimpleName();

    private final ConcurrentHashMap<String, FlightEventsListener> connectedApps = new ConcurrentHashMap<>();
    //private final ConcurrentHashMap<String, FlightshareClient> tlogUploaders = new ConcurrentHashMap<>();

    private final Context context;
    private final Flight flight;
    private final Follow followMe;

    public RcOutput getRcOutput() {
        return rcOutput;
    }

    private final RcOutput rcOutput;
    private final MavLinkMsgHandler mavLinkMsgHandler;
    private final ConnectionParameter connectionParameter;

    public FlightManager(Context context, ConnectionParameter connParams, final Handler handler, MavLinkServiceInterface mavLinkServiceInterface) {
        this.context = context;
        this.connectionParameter = connParams;

        MAVLinkClient mavClient = new MAVLinkClient(context, this, connParams, mavLinkServiceInterface);

        FlightInterfaces.Clock clock = new FlightInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };

        final FlightInterfaces.Handler dpHandler = new FlightInterfaces.Handler() {
            @Override
            public void removeCallbacks(Runnable thread) {
                handler.removeCallbacks(thread);
            }

            @Override
            public void post(Runnable thread) {
                handler.post(thread);
            }

            @Override
            public void postDelayed(Runnable thread, long timeout) {
                handler.postDelayed(thread, timeout);
            }
        };

        GCSCorePreferences gpscorePrefs = new GCSCorePreferences(context);

        this.flight = new FlightImpl(mavClient, clock, dpHandler, gpscorePrefs, new AndroidApWarningParser(), this);
        this.flight.getStreamRates().setRates(gpscorePrefs.getRates());

        this.mavLinkMsgHandler = new MavLinkMsgHandler(this.flight);

        this.followMe = new Follow(this.flight, dpHandler, new FusedLocation(context, handler));

        flight.addFlightListener(this);
        flight.getParameters().setParameterListener(this);
        flight.getMagnetometerCalibration().setListener(this);


        rcOutput = new RcOutput(flight, context);
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");

        flight.removeFlightListener(this);
        flight.getParameters().setParameterListener(null);
        flight.getMagnetometerCalibration().setListener(null);

        disconnect();

        connectedApps.clear();
      //  tlogUploaders.clear();

        if (followMe.isEnabled())
            followMe.toggleFollowMeState();
    }

    public void connect(String appId, FlightEventsListener listener) throws ConnectionException {
        if (listener == null || TextUtils.isEmpty(appId))
            return;

        connectedApps.put(appId, listener);

        MAVLinkClient mavClient = (MAVLinkClient) flight.getMavClient();

        if (!mavClient.isConnected()) {
            mavClient.openConnection();
        } else {
            if (flight.isConnected()) {

                if (flight.isConnectionAlive())
                    listener.onFlightEvent(FlightInterfaces.FlightEventsType.HEARTBEAT_FIRST, flight);
                else {
                    listener.onFlightEvent(FlightInterfaces.FlightEventsType.CONNECTED, flight);
                    listener.onFlightEvent(FlightInterfaces.FlightEventsType.HEARTBEAT_TIMEOUT, flight);
                }

                notifyConnected(appId, listener);
            }
        }

        mavClient.addLoggingFile(appId);
    }

    private void disconnect() {
        if (!connectedApps.isEmpty()) {
            for (String appId : connectedApps.keySet()) {
                try {
                    disconnect(appId);
                } catch (ConnectionException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    public int getConnectedAppsCount() {
        return connectedApps.size();
    }

    public void disconnect(String appId) throws ConnectionException {
        if (TextUtils.isEmpty(appId))
            return;

        Log.d(TAG, "Disconnecting client " + appId);
        FlightEventsListener listener = connectedApps.remove(appId);

        final MAVLinkClient mavClient = (MAVLinkClient) flight.getMavClient();
        if (listener != null) {
            mavClient.removeLoggingFile(appId);

            listener.onFlightEvent(FlightInterfaces.FlightEventsType.DISCONNECTED, flight);
            notifyDisconnected(appId, listener);
        }

        if (mavClient.isConnected() && connectedApps.isEmpty()) {
            mavClient.closeConnection();
        }
    }

    @Override
    public void notifyStartingConnection() {
        onFlightEvent(FlightInterfaces.FlightEventsType.CONNECTING, flight);
    }

    private void notifyConnected(String appId, FlightEventsListener listener) {
        if (TextUtils.isEmpty(appId) || listener == null)
            return;

        final FlightSharePreferences flightsharePrefs = listener.getFlightSharePreferences();

        //TODO: restore live upload functionality when issue
        // 'https://github.com/diydrones/droneapi-java/issues/2' is fixed.
        boolean isLiveUploadEnabled = false; //droneSharePrefs.isLiveUploadEnabled();
        if (flightsharePrefs != null && isLiveUploadEnabled && flightsharePrefs.areLoginCredentialsSet()) {

            Log.i(TAG, "Starting live upload for " + appId);
            /*
            try {
                DroneshareClient uploader = tlogUploaders.get(appId);
                if (uploader == null) {
                    uploader = new DroneshareClient();
                    tlogUploaders.put(appId, uploader);
                }

                uploader.connect(flightsharePrefs.getUsername(), flightsharePrefs.getPassword());
            } catch (Exception e) {
                Log.e(TAG, "DroneShare uploader error for " + appId, e);
            }
                */
        } else {
            Log.i(TAG, "Skipping live upload for " + appId);
        }
    }

    @Override
    public void notifyConnected() {
        // Start a new ga analytics session. The new session will be tagged
        // with the mavlink connection mechanism, as well as whether the user has an active droneshare account.
        GAUtils.startNewSession(context);

        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, FlightEventsListener> entry : connectedApps.entrySet()) {
                notifyConnected(entry.getKey(), entry.getValue());
            }
        }

        this.flight.notifyFlightEvent(FlightInterfaces.FlightEventsType.CHECKING_VEHICLE_LINK);
    }

    public void kickStartDroneShareUpload() {
        // See if we can at least do a delayed upload
        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, FlightEventsListener> entry : connectedApps.entrySet()) {
                kickStartDroneShareUpload(entry.getKey(), entry.getValue().getFlightSharePreferences());
            }
        }
    }

    private void kickStartDroneShareUpload(String appId, FlightSharePreferences prefs) {
        if (TextUtils.isEmpty(appId) || prefs == null)
            return;

     //   UploaderService.kickStart(context, appId, prefs);
    }

    private void notifyDisconnected(String appId, FlightEventsListener listener) {
        if (TextUtils.isEmpty(appId) || listener == null)
            return;

        kickStartDroneShareUpload(appId, listener.getFlightSharePreferences());

        /*
        DroneshareClient uploader = tlogUploaders.remove(appId);
        if (uploader != null) {
            try {
                uploader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error while closing the drone share upload handler.", e);
            }
        }
        */
    }

    @Override
    public void notifyDisconnected() {
        if (!connectedApps.isEmpty()) {
            for (Map.Entry<String, FlightEventsListener> entry : connectedApps.entrySet()) {
                notifyDisconnected(entry.getKey(), entry.getValue());
            }
        }

        this.flight.notifyFlightEvent(FlightInterfaces.FlightEventsType.DISCONNECTED);
    }

    @Override
    public void notifyReceivedData(MAVLinkPacket packet) {
        MAVLinkMessage receivedMsg = packet.unpack();
        this.mavLinkMsgHandler.receiveData(receivedMsg);

        if (!connectedApps.isEmpty()) {
            for (FlightEventsListener droneEventsListener : connectedApps.values()) {
                droneEventsListener.onReceivedMavLinkMessage(receivedMsg);
            }
        }

        /*
        if (!tlogUploaders.isEmpty()) {
            final byte[] packetData = packet.encodePacket();
            for (DroneshareClient uploader : tlogUploaders.values()) {
                try {
                    uploader.filterMavlink(uploader.interfaceNum, packetData);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        */
    }

    @Override
    public void onStreamError(String errorMsg) {
        if (connectedApps.isEmpty())
            return;

        for (FlightEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onConnectionFailed(errorMsg);
        }
    }

    public Flight getFlight() {
        return this.flight;
    }

    public Follow getFollowMe() {
        return followMe;
    }

    public boolean isConnected() {
        return flight.isConnected();
    }

    @Override
    public void onFlightEvent(FlightInterfaces.FlightEventsType event, Flight flight) {
        if (connectedApps.isEmpty())
            return;

        for (FlightEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onFlightEvent(event, flight);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        if (connectedApps.isEmpty())
            return;

        for (FlightEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onBeginReceivingParameters();
        }
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        if (connectedApps.isEmpty())
            return;

        for (FlightEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onParameterReceived(parameter, index, count);
        }
    }

    @Override
    public void onEndReceivingParameters() {
        if (connectedApps.isEmpty())
            return;

        for (FlightEventsListener droneEventsListener : connectedApps.values()) {
            droneEventsListener.onEndReceivingParameters();
        }
    }

    public ConnectionParameter getConnectionParameter() {
        return connectionParameter;
    }

    @Override
    public void onMessageLogged(int mavSeverity, String message) {
        if (connectedApps.isEmpty())
            return;

        final int logLevel;
        switch (mavSeverity) {
            case MAV_SEVERITY.MAV_SEVERITY_ALERT:
            case MAV_SEVERITY.MAV_SEVERITY_CRITICAL:
            case MAV_SEVERITY.MAV_SEVERITY_EMERGENCY:
            case MAV_SEVERITY.MAV_SEVERITY_ERROR:
                logLevel = Log.ERROR;
                break;

            case MAV_SEVERITY.MAV_SEVERITY_WARNING:
                logLevel = Log.WARN;
                break;

            case MAV_SEVERITY.MAV_SEVERITY_NOTICE:
                logLevel = Log.INFO;
                break;

            default:
            case MAV_SEVERITY.MAV_SEVERITY_INFO:
                logLevel = Log.VERBOSE;
                break;

            case MAV_SEVERITY.MAV_SEVERITY_DEBUG:
                logLevel = Log.DEBUG;
                break;
        }

        for (FlightEventsListener listener : connectedApps.values()) {
            listener.onMessageLogged(logLevel, message);
        }
    }

    @Override
    public void onCalibrationCancelled() {
        if(connectedApps.isEmpty())
            return;

        for(FlightEventsListener listener: connectedApps.values())
            listener.onCalibrationCancelled();
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        if(connectedApps.isEmpty())
            return;

        for(FlightEventsListener listener: connectedApps.values())
            listener.onCalibrationProgress(progress);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        if(connectedApps.isEmpty())
            return;

        for(FlightEventsListener listener: connectedApps.values())
            listener.onCalibrationCompleted(report);
    }
}
