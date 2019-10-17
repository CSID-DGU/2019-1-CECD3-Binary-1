package huins.ex.proto;

import android.os.Bundle;
import android.os.RemoteException;

/**
 * Created by suhak on 15. 6. 26.
 */
public class FlightObserver extends IObserver.Stub {

    private final Flight flight;

    public FlightObserver(Flight flight) {
        this.flight = flight;
    }

    @Override
    public void onAttributeUpdated(String attributeEvent, Bundle eventExtras) throws
            RemoteException {
        flight.notifyAttributeUpdated(attributeEvent, eventExtras);
    }
}

