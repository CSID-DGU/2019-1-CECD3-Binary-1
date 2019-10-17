package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.common.msg_command_long;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.core.model.Flight;

public class MavLinkTakeoff {
    public static void sendTakeoff(Flight flight, double alt) {
        msg_command_long msg = new msg_command_long();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;

        msg.param7 = (float) alt;

        flight.getMavClient().sendMavPacket(msg.pack());
    }
}
