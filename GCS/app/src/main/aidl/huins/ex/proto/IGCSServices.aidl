// IGCSServices.aidl
package huins.ex.proto;

import huins.ex.service.GCSinterface.IFlightInterface;
import huins.ex.proto.IApiListener;

/**
* Used to establish connection with a drone.
*/
interface IGCSServices {

    int getServiceVersionCode();

    void releaseFlightApi(IFlightInterface flightinterface);

    int getApiVersionCode();

    IFlightInterface registerFlightApi(IApiListener listener, String appId);

    Bundle[] getConnectedApps(String requesterId);
}
