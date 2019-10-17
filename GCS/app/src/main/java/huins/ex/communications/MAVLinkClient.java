package huins.ex.communications;

import android.content.Context;

import java.io.File;
import java.util.Date;

import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.core.MAVLink.MAVLinkStreams;
import huins.ex.core.MAVLink.connection.MavLinkConnectionListener;
import huins.ex.core.MAVLink.connection.MavLinkConnectionTypes;
import huins.ex.data.SessionDB;
import huins.ex.proto.ConnectionParameter;
import huins.ex.service.MavLinkServiceInterface;
import huins.ex.util.DirectoryPath;
import huins.ex.util.File.FileUtils;

/**
 * Provide a common class for some ease of use functionality
 */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

    private final static String TAG = MAVLinkClient.class.getSimpleName();

    private static final String TLOG_PREFIX = "log";

    /**
     * Maximum possible sequence number for a packet.
     */
    private static final int MAX_PACKET_SEQUENCE = 255;

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {

        @Override
        public void onStartingConnection() {
            listener.notifyStartingConnection();
        }

        @Override
        public void onConnect(long connectionTime) {
            startLoggingThread(connectionTime);
            listener.notifyConnected();
        }

        @Override
        public void onReceivePacket(final MAVLinkPacket packet) {
            listener.notifyReceivedData(packet);
        }

        @Override
        public void onDisconnect(long disconnectTime) {
            listener.notifyDisconnected();
            closeConnection();
        }

        @Override
        public void onComError(final String errMsg) {
            if (errMsg != null) {
                listener.onStreamError(errMsg);
            }
        }
    };

    private final MAVLinkStreams.MavlinkInputStream listener;
    private final MavLinkServiceInterface mavLinkServiceInterface;
    private final SessionDB sessionDB;
    private final Context context;

    private int packetSeqNumber = 0;
    private final ConnectionParameter connParams;

    public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener,
                         ConnectionParameter connParams, MavLinkServiceInterface serviceApi) {
        this.context = context;
        this.listener = listener;
        this.mavLinkServiceInterface = serviceApi;
        this.connParams = connParams;
        this.sessionDB = new SessionDB(context);
    }

    @Override
    public void openConnection() {
        if (this.connParams == null)
            return;

        final String tag = toString();
        final int connectionStatus = mavLinkServiceInterface.getConnectionStatus(this.connParams, tag);
        if (connectionStatus == MavLinkConnection.MAVLINK_DISCONNECTED
                || connectionStatus == MavLinkConnection.MAVLINK_CONNECTING) {
            mavLinkServiceInterface.connectMavLink(this.connParams, tag, mConnectionListener);
        }
    }

    @Override
    public void closeConnection() {
        if (this.connParams == null)
            return;

        final String tag = toString();
        if (mavLinkServiceInterface.getConnectionStatus(this.connParams, tag) == MavLinkConnection.MAVLINK_CONNECTED) {
            mavLinkServiceInterface.disconnectMavLink(this.connParams, tag);
            stopLoggingThread(System.currentTimeMillis());
            listener.notifyDisconnected();
        }
    }

    @Override
    public void sendMavPacket(MAVLinkPacket pack) {
        if (this.connParams == null) {
            return;
        }

        pack.seq = packetSeqNumber;

        if(mavLinkServiceInterface.sendData(this.connParams, pack)) {
            packetSeqNumber = (packetSeqNumber + 1) % (MAX_PACKET_SEQUENCE + 1);
        }
    }

    @Override
    public boolean isConnected() {
        return this.connParams != null
                && mavLinkServiceInterface.getConnectionStatus(this.connParams, toString()) == MavLinkConnection.MAVLINK_CONNECTED;
    }

    public boolean isConnecting(){
        return this.connParams != null && mavLinkServiceInterface.getConnectionStatus(this.connParams,
                toString()) == MavLinkConnection.MAVLINK_CONNECTING;
    }

    @Override
    public void toggleConnectionState() {
        if (isConnected()) {
            closeConnection();
        } else {
            openConnection();
        }
    }

    private File getTLogDir(String appId) {
        return DirectoryPath.getTLogPath(this.context, appId);
    }

    private File getTempTLogFile(String appId, long connectionTimestamp) {
        return new File(getTLogDir(appId), getTLogFilename(connectionTimestamp));
    }

    private String getTLogFilename(long connectionTimestamp) {
        return TLOG_PREFIX + "_" + MavLinkConnectionTypes.getConnectionTypeLabel(this.connParams.getConnectionType()) +
                "_" + FileUtils.getTimeStamp(connectionTimestamp) + FileUtils.TLOG_FILENAME_EXT;
    }

    public void addLoggingFile(String appId){
        if(isConnecting() || isConnected()) {
            final File logFile = getTempTLogFile(appId, System.currentTimeMillis());
            mavLinkServiceInterface.addLoggingFile(this.connParams, appId, logFile.getAbsolutePath());
        }
    }

    public void removeLoggingFile(String appId){
        if(isConnecting() || isConnected()){
            mavLinkServiceInterface.removeLoggingFile(this.connParams, appId);
        }
    }

    private void startLoggingThread(long startTime) {
        //log into the database the connection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.startSession(new Date(startTime), connectionType);
    }

    private void stopLoggingThread(long stopTime) {
        //log into the database the disconnection time.
        final String connectionType = MavLinkConnectionTypes.getConnectionTypeLabel(connParams.getConnectionType());
        this.sessionDB.endSession(new Date(stopTime), connectionType, new Date());
    }
}
