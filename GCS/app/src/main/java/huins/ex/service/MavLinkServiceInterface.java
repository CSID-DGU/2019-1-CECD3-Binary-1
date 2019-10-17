package huins.ex.service;

import java.lang.ref.SoftReference;

import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.communications.AndroidMavLinkConnection;
import huins.ex.communications.MavLinkConnection;
import huins.ex.core.MAVLink.connection.MavLinkConnectionListener;
import huins.ex.proto.ConnectionParameter;

/**
 * Created by suhak on 15. 7. 14.
 */
public class MavLinkServiceInterface {
    private final SoftReference<GCSFlightService> mServiceRef;

    public MavLinkServiceInterface(GCSFlightService service) {
        mServiceRef = new SoftReference<>(service);
    }

    private GCSFlightService getService() {
        GCSFlightService service = mServiceRef.get();
        if (service == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return service;
    }

    public boolean sendData(ConnectionParameter connParams, MAVLinkPacket packet) {
        final AndroidMavLinkConnection mavConnection = getService().mavConnections.get(connParams.getUniqueId());
        if (mavConnection == null) return false;

        if (mavConnection.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
            mavConnection.sendMavPacket(packet);
            return true;
        }

        return false;
    }

    public int getConnectionStatus(ConnectionParameter connParams, String tag) {
        final AndroidMavLinkConnection mavConnection = getService().mavConnections.get(connParams.getUniqueId());
        if (mavConnection == null || !mavConnection.hasMavLinkConnectionListener(tag)) {
            return MavLinkConnection.MAVLINK_DISCONNECTED;
        }

        return mavConnection.getConnectionStatus();
    }

    public void connectMavLink(ConnectionParameter connParams, String tag, MavLinkConnectionListener listener) {
        getService().connectMAVConnection(connParams, tag, listener);
    }

    public void addLoggingFile(ConnectionParameter connParams, String tag, String loggingFilePath){
        getService().addLoggingFile(connParams, tag, loggingFilePath);
    }

    public void removeLoggingFile(ConnectionParameter connParams, String tag){
        getService().removeLoggingFile(connParams, tag);
    }

    public void disconnectMavLink(ConnectionParameter connParams, String tag) {
        getService().disconnectMAVConnection(connParams, tag);
    }
}

