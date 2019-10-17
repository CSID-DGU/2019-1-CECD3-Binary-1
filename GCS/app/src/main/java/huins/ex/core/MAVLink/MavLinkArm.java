package huins.ex.core.MAVLink;


import huins.ex.com.MAVLink.common.msg_command_long;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.core.model.Flight;

public class MavLinkArm {

	public static void sendArmMessage(Flight flight, boolean arm) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = flight.getSysid();
		msg.target_component = flight.getCompid();

		msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
		msg.param1 = arm ? 1 : 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		flight.getMavClient().sendMavPacket(msg.pack());
	}

}
