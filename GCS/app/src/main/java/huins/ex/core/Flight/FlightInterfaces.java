package huins.ex.core.Flight;


import huins.ex.core.MAVLink.WaypointManager;
import huins.ex.core.model.Flight;
import huins.ex.core.parameters.Parameter;

public class FlightInterfaces {

	/**
	 * Sets of drone events used for broadcast throughout the app.
	 */
	public enum FlightEventsType {
        /**
         * Denotes vehicle altitude change event.
         */
        ALTITUDE,

		/**
         *
         */
		ORIENTATION,

		/**
         * Denotes vehicle speed change event.
         */
		SPEED,

		/**
         *
         */
		BATTERY,

		/**
         *
         */
		GUIDEDPOINT,

		/**
         *
         */
		NAVIGATION,

		/**
         * Denotes vehicle attitude change event.
         */
		ATTITUDE,

		/**
         *
         */
		RADIO,

		/**
         *
         */
		RC_IN,

		/**
         *
         */
		RC_OUT,

		/**
         *
         */
		ARMING,

		/**
         *
         */
		AUTOPILOT_WARNING,

		/**
         *
         */
		MODE,

		/**
         *
         */
		STATE,

		/**
         *
         */
		MISSION_UPDATE,

		/**
         *
         */
		MISSION_RECEIVED,

		/**
         *
         */
		TYPE,

		/**
         *
         */
		HOME,

		/**
		 * Broadcast to notify of updates to the drone's gps location.
		 */
		GPS,

		/**
         *
         */
		GPS_FIX,

		/**
         *
         */
		GPS_COUNT,

		/**
         *
         */
		CALIBRATION_IMU,

		/**
         *
         */
		CALIBRATION_TIMEOUT,

		/**
         *
         */
		HEARTBEAT_TIMEOUT,

		/**
         *
         */
		HEARTBEAT_FIRST,

		/**
         *
         */
		HEARTBEAT_RESTORED,

		/**
         *
         */
		DISCONNECTED,

		/**
         * Successful connection event.
         */
		CONNECTED,

        /**
         * Connection initiated event.
         */
        CONNECTING,

        /**
         * Vehicle link is being validated.
         */
        CHECKING_VEHICLE_LINK,

        /**
         * Connection failed event.
         */
        CONNECTION_FAILED,

		/**
         *
         */
		MISSION_SENT,

		/**
         *
         */
		ARMING_STARTED,

		/**
         *
         */
		INVALID_POLYGON,

		/**
         *
         */
		MISSION_WP_UPDATE,

		/**
		 * 'Follow' mode has been enabled.
		 */
		FOLLOW_START,

		/**
		 * 'Follow' mode has been disabled.
		 */
		FOLLOW_STOP,
		
		/**
		 * 'Follow' state has been updated.
		 */
		FOLLOW_UPDATE,

		/**
         * 
         */
		FOLLOW_CHANGE_TYPE,

		/**
		 *
		 */
		PARAMETERS_DOWNLOADED,

		/**
		 *
		 */
		WARNING_SIGNAL_WEAK,
		/**
		 * Announces that a new version for the firmware has been received
		 */
		FIRMWARE,

		/**
		 * Warn that the drone has no gps signal
		 */
		WARNING_NO_GPS, 
		
		/**
		 * New magnetometer data has been received
		 */
		MAGNETOMETER,
		
		/**
		 * The drone camera footprints has been updated
		 */
		FOOTPRINT,

        /**
         * The gopro status was updated.
         */
        GOPRO_STATUS_UPDATE,

		/**
		 * The ekf status was updated.
		 */
		EKF_STATUS_UPDATE,

		/**
		 * The horizontal position is ok, and the home position is available.
		 */
		EKF_POSITION_STATE_UPDATE
	}

	public interface OnFlightListener {
		public void onFlightEvent(FlightEventsType event, Flight flight);
	}

	public interface OnParameterManagerListener {
		public void onBeginReceivingParameters();

		public void onParameterReceived(Parameter parameter, int index, int count);

		public void onEndReceivingParameters();
	}

	public interface OnWaypointManagerListener {
		public void onBeginWaypointEvent(WaypointManager.WaypointEvent_Type wpEvent);

		public void onWaypointEvent(WaypointManager.WaypointEvent_Type wpEvent, int index, int count);

		public void onEndWaypointEvent(WaypointManager.WaypointEvent_Type wpEvent);
	}

	public interface Clock {

		long elapsedRealtime();

	}

	public interface Handler {

		void removeCallbacks(Runnable thread);

        void post(Runnable thread);

		void postDelayed(Runnable thread, long timeout);

	}

}
