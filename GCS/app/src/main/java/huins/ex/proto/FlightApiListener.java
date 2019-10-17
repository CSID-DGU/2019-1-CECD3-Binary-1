package huins.ex.proto;

import android.os.RemoteException;

import huins.ex.BuildConfig;
import huins.ex.proto.connection.ConnectionResult;
import huins.ex.util.constants.ConstantsBase;

/**
 * Created by suhak on 15. 6. 26.
 */
public class FlightApiListener extends IApiListener.Stub {

    private final Flight flight;

    public FlightApiListener(Flight flight){
        this.flight = flight;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) throws RemoteException {
        flight.notifyFlightConnectionFailed(connectionResult);
    }

    @Override
    public int getClientVersionCode() throws RemoteException {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public int getApiVersionCode(){
        return ConstantsBase.GCS_VERSION;
    }
}
