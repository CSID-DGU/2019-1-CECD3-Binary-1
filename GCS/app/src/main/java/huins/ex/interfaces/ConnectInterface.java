package huins.ex.interfaces;

import android.os.Bundle;

import huins.ex.proto.ConnectionParameter;
import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;

import static huins.ex.proto.action.ConnectionActions.ACTION_CONNECT;
import static huins.ex.proto.action.ConnectionActions.ACTION_DISCONNECT;
import static huins.ex.proto.action.ConnectionActions.EXTRA_CONNECT_PARAMETER;

/**
 * Created by suhak on 15. 7. 16.
 */

public class ConnectInterface {
    public ConnectInterface() {
    }

    public static boolean connect(Flight flight, ConnectionParameter parameter) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_CONNECT_PARAMETER, parameter);
        Action connectAction = new Action(ACTION_CONNECT, params);
        return flight.performAsyncAction(connectAction);
    }

    public static boolean disconnect(Flight flight) {
        return flight.performAsyncAction(new Action(ACTION_DISCONNECT));
    }
}
