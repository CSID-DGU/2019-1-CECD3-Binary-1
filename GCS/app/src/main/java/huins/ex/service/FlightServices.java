package huins.ex.service;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import huins.ex.BuildConfig;
import huins.ex.proto.ConnectionParameter;
import huins.ex.proto.IApiListener;
import huins.ex.proto.IGCSServices;
import huins.ex.proto.gcs.event.GCSEvent;
import huins.ex.service.GCSinterface.FlightInterface;
import huins.ex.service.GCSinterface.IFlightInterface;
import huins.ex.util.VersionUtils;

/**
 * Created by suhak on 15. 7. 14.
 */
public class FlightServices extends IGCSServices.Stub {

    private final static String TAG = FlightServices.class.getSimpleName();

    private GCSFlightService serviceRef;

    FlightServices(GCSFlightService service) {
        serviceRef = service;
    }

    void destroy(){
        serviceRef = null;
    }

    @Override
    public int getServiceVersionCode() throws RemoteException {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public int getApiVersionCode() throws RemoteException {
        return VersionUtils.LIB_VERSION;
    }

    @Override
    public IFlightInterface registerFlightApi(IApiListener listener, String appId) throws RemoteException {
        return serviceRef.registerFlightApi(listener, appId);
    }

    @Override
    public Bundle[] getConnectedApps(String requesterId) throws RemoteException {
        Log.d(TAG, "List of connected apps request from " + requesterId);

        List<Bundle> appsInfo = new ArrayList<>();
        for(FlightInterface flightInterface : serviceRef.flightinterfaceStore.values()){
            if(flightInterface.isConnected()){
                FlightManager droneManager = flightInterface.getFlightManager();
                if(droneManager != null) {
                    final ConnectionParameter droneParams = flightInterface.getFlightManager().getConnectionParameter();
                    final ConnectionParameter sanitizedParams = new ConnectionParameter(droneParams.getConnectionType(),
                            droneParams.getParamsBundle(), null);

                    Bundle info = new Bundle();
                    info.putString(GCSEvent.EXTRA_APP_ID, flightInterface.getOwnerId());
                    info.putParcelable(GCSEvent.EXTRA_VEHICLE_CONNECTION_PARAMETER, sanitizedParams);

                    appsInfo.add(info);
                }
            }
        }

        return appsInfo.toArray(new Bundle[appsInfo.size()]);
    }

    @Override
    public void releaseFlightApi(IFlightInterface flightInterface) throws RemoteException {
        Log.d(TAG, "Releasing acquired drone api handle.");
        if(flightInterface instanceof IFlightInterface) {
            serviceRef.releaseFlightInterface(((FlightInterface) flightInterface).getOwnerId());
        }
    }
}
