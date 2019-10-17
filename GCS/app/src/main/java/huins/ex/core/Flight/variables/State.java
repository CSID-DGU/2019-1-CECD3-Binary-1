package huins.ex.core.Flight.variables;

import huins.ex.com.MAVLink.Messages.ApmModes;
import huins.ex.com.MAVLink.ardupilotmega.msg_ekf_status_report;
import huins.ex.com.MAVLink.enums.EKF_STATUS_FLAGS;
import huins.ex.core.Flight.FlightInterfaces.Clock;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightInterfaces.Handler;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkModes;
import huins.ex.core.model.AutopilotWarningParser;
import huins.ex.core.model.Flight;


public class State extends FlightVariable {
	private static final long ERROR_TIMEOUT = 5000l;

    private final AutopilotWarningParser warningParser;

	private msg_ekf_status_report ekfStatus;
	private boolean isEkfPositionOk;

	private String errorId;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private final Clock clock;

	private final Handler watchdog;
	private final Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			resetWarning();
		}
	};

	public State(Flight mflight, Clock clock, Handler handler, AutopilotWarningParser warningParser) {
		super(mflight);
		this.clock = clock;
		this.watchdog = handler;
        this.warningParser = warningParser;
        this.errorId = warningParser.getDefaultWarning();
		resetFlightStartTime();
	}

	public boolean isArmed() {
		return armed;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public ApmModes getMode() {
		return mode;
	}

	public String getErrorId() {
		return errorId;
	}

	public void setIsFlying(boolean newState) {
		if (newState != isFlying) {
			isFlying = newState;
			mflight.notifyFlightEvent(FlightEventsType.STATE);

			if (isFlying) {
				resetFlightStartTime();
			}
		}
	}

    public boolean parseAutopilotError(String errorMsg){
        String parsedError = warningParser.parseWarning(mflight, errorMsg);
        if(parsedError == null || parsedError.trim().isEmpty())
            return false;

        if (!parsedError.equals(this.errorId)) {
            this.errorId = parsedError;
            mflight.notifyFlightEvent(FlightEventsType.AUTOPILOT_WARNING);
        }

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, ERROR_TIMEOUT);
        return true;
    }

    public void repeatWarning(){
        if(errorId == null || errorId.length() == 0 || errorId.equals(warningParser.getDefaultWarning()))
            return;

        watchdog.removeCallbacks(watchdogCallback);
        this.watchdog.postDelayed(watchdogCallback, ERROR_TIMEOUT);
    }

	public void setArmed(boolean newState) {
		if (this.armed != newState) {
			this.armed = newState;
			mflight.notifyFlightEvent(FlightEventsType.ARMING);

			if (newState) {
				mflight.getWaypointManager().getWaypoints();
			}else{
				if (mode == ApmModes.ROTOR_RTL || mode == ApmModes.ROTOR_LAND) {
					changeFlightMode(ApmModes.ROTOR_LOITER);  // When disarming set the mode back to loiter so we can do a takeoff in the future.					
				}
			}
		}

		checkEkfPositionState(this.ekfStatus);
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			mflight.notifyFlightEvent(FlightEventsType.MODE);
		}
	}

	public void changeFlightMode(ApmModes mode) {
		if (ApmModes.isValid(mode)) {
			MavLinkModes.changeFlightMode(mflight, mode);
		}
	}

	private void resetWarning() {
		String defaultWarning = warningParser.getDefaultWarning();
        if(defaultWarning == null)
            defaultWarning = "";

        if (!defaultWarning.equals(this.errorId)) {
            this.errorId = defaultWarning;
            mflight.notifyFlightEvent(FlightEventsType.AUTOPILOT_WARNING);
        }
	}

	// flightTimer
	// ----------------

	private void resetFlightStartTime() {
		startTime = clock.elapsedRealtime();
	}

	public long getFlightStartTime() {
        return startTime;
	}

	public msg_ekf_status_report getEkfStatus() {
		return ekfStatus;
	}

	public void setEkfStatus(msg_ekf_status_report ekfState) {
		if(this.ekfStatus == null || !areEkfStatusEquals(this.ekfStatus, ekfState)) {
			this.ekfStatus = ekfState;
			mflight.notifyFlightEvent(FlightEventsType.EKF_STATUS_UPDATE);
		}
	}

	private void checkEkfPositionState(msg_ekf_status_report ekfStatus){
		if(ekfStatus == null)
			return;

		final short flags = ekfStatus.flags;

		final boolean isOk = this.armed
				? (flags & EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS) != 0
				&& (flags & EKF_STATUS_FLAGS.EKF_CONST_POS_MODE) == 0
				: (flags & EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS) != 0
				|| (flags & EKF_STATUS_FLAGS.EKF_PRED_POS_HORIZ_ABS) != 0;

		if(isEkfPositionOk != isOk){
			isEkfPositionOk = isOk;
			mflight.notifyFlightEvent(FlightEventsType.EKF_POSITION_STATE_UPDATE);
		}
	}

	private static boolean areEkfStatusEquals(msg_ekf_status_report one, msg_ekf_status_report two) {
        return one == two || !(one == null || two == null) && one.toString().equals(two.toString());
    }

	public boolean isEkfPositionOk() {
		return isEkfPositionOk;
	}
}