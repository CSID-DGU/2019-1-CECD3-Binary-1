package huins.ex.core.gcs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import huins.ex.core.MAVLink.MavLinkHeartbeat;
import huins.ex.core.model.Flight;

/**
 * This class is used to send periodic heartbeat messages to the drone.
 */
public class GCSHeartbeat {

	/**
	 * This is the drone to send the heartbeat message to.
	 */
	private final Flight flight;

	/**
	 * This is the heartbeat period in seconds.
	 */
	private final int period;

	/**
	 * ScheduledExecutorService used to periodically schedule the heartbeat.
	 */
	private ScheduledExecutorService heartbeatExecutor;

	/**
	 * Runnable used to send the heartbeat.
	 */
	private final Runnable heartbeatRunnable = new Runnable() {
		@Override
		public void run() {
			MavLinkHeartbeat.sendMavHeartbeat(flight);
		}
	};

	public GCSHeartbeat(Flight flight, int freqHz) {
		this.flight = flight;
		this.period = freqHz;
	}

	/**
	 * Set the state of the heartbeat.
	 * 
	 * @param active
	 *            true to activate the heartbeat, false to deactivate it
	 */
	public void setActive(boolean active) {
		if (active) {
            if(heartbeatExecutor == null || heartbeatExecutor.isShutdown()) {
                heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
                heartbeatExecutor.scheduleWithFixedDelay(heartbeatRunnable, 0, period, TimeUnit.SECONDS);
            }
		} else if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
			heartbeatExecutor.shutdownNow();
			heartbeatExecutor = null;
		}
	}
}
