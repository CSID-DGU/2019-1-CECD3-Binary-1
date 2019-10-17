package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.common.msg_mission_ack;
import huins.ex.com.MAVLink.common.msg_mission_count;
import huins.ex.com.MAVLink.common.msg_mission_request;
import huins.ex.com.MAVLink.common.msg_mission_request_list;
import huins.ex.com.MAVLink.common.msg_mission_set_current;
import huins.ex.com.MAVLink.enums.MAV_MISSION_RESULT;
import huins.ex.core.model.Flight;

public class MavLinkWaypoint {

	public static void sendAck(Flight flight) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = flight.getSysid();
		msg.target_component = flight.getCompid();
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
		flight.getMavClient().sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(Flight flight, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = flight.getSysid();
		msg.target_component = flight.getCompid();
		msg.seq = (short) index;
		flight.getMavClient().sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(Flight flight) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = flight.getSysid();
		msg.target_component = flight.getCompid();
		flight.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(Flight flight, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = flight.getSysid();
		msg.target_component = flight.getCompid();
		msg.count = (short) count;
		flight.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(Flight flight, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = flight.getSysid();
		msg.target_component = flight.getCompid();
		msg.seq = i;
		flight.getMavClient().sendMavPacket(msg.pack());
	}

}
