package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;

public class Battery extends FlightVariable {
	private double battVolt = -1;
	private double battRemain = -1;
	private double battCurrent = -1;

	public Battery(Flight mflight) {
		super(mflight);
	}

	public double getBattVolt() {
		return battVolt;
	}

	public double getBattRemain() {
		return battRemain;
	}

	public double getBattCurrent() {
		return battCurrent;
	}

	public Double getBattDischarge() {
		Parameter battCap = mflight.getParameters().getParameter("BATT_CAPACITY");
		if (battCap == null || battRemain == -1) {
			return null;			
		}
		return (1-battRemain/100.0)*battCap.value; 
	}
	
	public void setBatteryState(double battVolt, double battRemain, double battCurrent) {
		if (this.battVolt != battVolt || this.battRemain != battRemain || this.battCurrent != battCurrent) {
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			mflight.notifyFlightEvent(FlightEventsType.BATTERY);
		}
	}

}