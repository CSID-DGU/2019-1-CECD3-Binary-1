package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.OnFlightListener;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkStreamRates;
import huins.ex.core.model.Flight;

public class StreamRates extends FlightVariable implements OnFlightListener {

    private Rates rates;

	public StreamRates(Flight mflight) {
		super(mflight);
		mflight.addFlightListener(this);
	}

    public void setRates(Rates rates) {
        this.rates = rates;
    }

    @Override
	public void onFlightEvent(FlightEventsType event, Flight flight) {
		switch (event) {
        case CONNECTED:
		case HEARTBEAT_FIRST:
		case HEARTBEAT_RESTORED:
			setupStreamRatesFromPref();
			break;
		default:
			break;
		}
	}

	public void setupStreamRatesFromPref() {
        if(rates == null)
            return;

		MavLinkStreamRates.setupStreamRates(mflight.getMavClient(), mflight.getSysid(),
				mflight.getCompid(), rates.extendedStatus, rates.extra1, rates.extra2,
				rates.extra3, rates.position, rates.rcChannels, rates.rawSensors,
				rates.rawController);
	}

    public static class Rates {
        public int extendedStatus;
        public int extra1;
        public int extra2;
        public int extra3;
        public int position;
        public int rcChannels;
        public int rawSensors;
        public int rawController;
    }

}
