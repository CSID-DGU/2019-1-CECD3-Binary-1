package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.common.msg_command_long;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.core.model.Flight;

/**
 * Created by ssw on 2016-06-09.
 */
public class MavLinkCameraResolution {
    public static void sendCameraResolution(Flight flight, int resolutionType) {
        if(flight == null)
            return;
        msg_command_long msg = new msg_command_long();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.command = MAV_CMD.MAV_CMD_VIDEO_START_CAPTURE;

        msg.param3 = resolutionType;

        flight.getMavClient().sendMavPacket(msg.pack());
    }
}
