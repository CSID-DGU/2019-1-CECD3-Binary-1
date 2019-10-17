package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.MAVLinkPacket;
import huins.ex.com.MAVLink.common.msg_heartbeat;
import huins.ex.com.MAVLink.enums.MAV_AUTOPILOT;
import huins.ex.com.MAVLink.enums.MAV_TYPE;
import huins.ex.core.model.Flight;

/**
 * This class contains logic used to send an heartbeat to a
 */
public class MavLinkHeartbeat {

	/**
	 * This is the msg heartbeat used to check the drone is present, and
	 * responding.
	 */
	private static final msg_heartbeat sMsg = new msg_heartbeat();
	static {
		sMsg.type = MAV_TYPE.MAV_TYPE_GCS;
		sMsg.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC;
	}

	/**
	 * This is the mavlink packet obtained from the msg heartbeat, and used for
	 * actual communication.
	 */
	private static final MAVLinkPacket sMsgPacket = sMsg.pack();

	/**
	 * object.
	 * 
	 * @param flight
	 *            flight to send the heartbeat to
	 */
	public static void sendMavHeartbeat(Flight flight) {
		if (flight != null)
			flight.getMavClient().sendMavPacket(sMsgPacket);
	}

}
