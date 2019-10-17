package huins.ex.core.Flight.variables;

import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;

public class Speed extends FlightVariable {

	private double verticalSpeed = 0;
	private double groundSpeed = 0;
	private double airSpeed = 0;
	private double targetSpeed = 0;

	public Speed(Flight mflight) {
		super(mflight);
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public double getGroundSpeed() {
		return groundSpeed;
	}

	public double getAirSpeed() {
		return airSpeed;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = (aspd_error + airSpeed);
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
        boolean speedUpdated = false;
        if(this.groundSpeed != groundSpeed) {
            this.groundSpeed = groundSpeed;
            speedUpdated = true;
        }

        if(this.airSpeed != airSpeed){
            this.airSpeed = airSpeed;
            speedUpdated = true;
        }

        if(this.verticalSpeed != climb){
            this.verticalSpeed = climb;
            speedUpdated = true;
        }

        if(speedUpdated) {
            mflight.notifyFlightEvent(FlightInterfaces.FlightEventsType.SPEED);
        }
	}

	public double getSpeedParameter(){
		Parameter param = mflight.getParameters().getParameter("WPNAV_SPEED");
		if (param == null ) {
			return -1;
		}else{
			return (param.value/100);
		}
			
	}
}
