package huins.ex.proto;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import huins.ex.proto.connection.FlightSharePreferences;
import huins.ex.util.constants.ConnectionType;

/**
 * Created by suhak on 15. 6. 11.
 */
public class ConnectionParameter implements Parcelable {

    private final int connectionType;
    private final Bundle paramsBundle;
    private final FlightSharePreferences flightsharePreferences;

    public ConnectionParameter(int connectionType, Bundle paramsBundle, FlightSharePreferences flightSharePrefs){
        this.connectionType = connectionType;
        this.paramsBundle = paramsBundle;
        this.flightsharePreferences = flightSharePrefs;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public Bundle getParamsBundle() {
        return paramsBundle;
    }

    public FlightSharePreferences getFlightSharePreferences() {
        return flightsharePreferences;
    }

    public String getUniqueId(){
        final String uniqueId;
        switch(connectionType){

            case ConnectionType.TYPE_UDP:
                int udpPort = ConnectionType.DEFAULT_UDP_SERVER_PORT;
                if(paramsBundle != null){
                    udpPort = paramsBundle.getInt(ConnectionType.EXTRA_UDP_SERVER_PORT, udpPort);
                }
                uniqueId = "udp." + udpPort;
                break;


            case ConnectionType.TYPE_TCP:
                String tcpIp = null;
                int tcpPort = ConnectionType.DEFAULT_TCP_SERVER_PORT;
                if(paramsBundle != null){
                    tcpIp = paramsBundle.getString(ConnectionType.EXTRA_TCP_SERVER_IP);
                    tcpPort = paramsBundle.getInt(ConnectionType.EXTRA_TCP_SERVER_PORT, tcpPort);
                }

                uniqueId = "tcp"  + "." + tcpPort + (tcpIp == null ? "" : "." + tcpIp);
                break;
            case ConnectionType.TYPE_USB:
                uniqueId = "usb";
                break;
            default:
                uniqueId = "";
                break;
        }

        return uniqueId;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof ConnectionParameter)) return false;

        ConnectionParameter that = (ConnectionParameter) o;
        return getUniqueId().equals(that.getUniqueId());
    }

    @Override
    public int hashCode(){
        return getUniqueId().hashCode();
    }

    @Override
    public String toString() {
        String toString = "ConnectionParameter{" +
                "connectionType=" + connectionType +
                ", paramsBundle=[";

        if (paramsBundle != null && !paramsBundle.isEmpty()) {
            boolean isFirst = true;
            for (String key : paramsBundle.keySet()) {
                if (isFirst)
                    isFirst = false;
                else
                    toString += ", ";

                toString += key + "=" + paramsBundle.get(key);
            }
        }

        toString += "]}";
        return toString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.connectionType);
        dest.writeBundle(paramsBundle);
        dest.writeParcelable(this.flightsharePreferences, 0);
    }

    private ConnectionParameter(Parcel in) {
        this.connectionType = in.readInt();
        paramsBundle = in.readBundle();
        this.flightsharePreferences = in.readParcelable(FlightSharePreferences.class.getClassLoader());
    }

    public static final Creator<ConnectionParameter> CREATOR = new Creator<ConnectionParameter>() {
        public ConnectionParameter createFromParcel(Parcel source) {
            return new ConnectionParameter(source);
        }

        public ConnectionParameter[] newArray(int size) {
            return new ConnectionParameter[size];
        }
    };
}
