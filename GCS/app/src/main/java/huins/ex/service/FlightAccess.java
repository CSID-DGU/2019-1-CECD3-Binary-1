package huins.ex.service;

import android.os.Binder;

import java.util.ArrayList;
import java.util.List;

import huins.ex.service.GCSinterface.FlightInterface;

/**
 * Created by suhak on 15. 7. 14.
 */
public class FlightAccess extends Binder {

    private final GCSFlightService serviceRef;

    FlightAccess(GCSFlightService service) {
        serviceRef = service;
    }

    public List<FlightInterface> getDroneApiList() {
        return new ArrayList<>(serviceRef.flightinterfaceStore.values());
    }
}
