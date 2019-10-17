package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.ardupilotmega.msg_gimbal_control;
import huins.ex.core.model.Flight;

/**
 * Created by ssw on 2016-06-14.
 */
public class MavLinkGimbalControl {
    public static void sendGimbalAxisYaw(Flight flight, float yaw) {
        if(flight == null)
            return;
        msg_gimbal_control msg = new msg_gimbal_control();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();

        msg.demanded_rate_x = yaw;
        flight.getMavClient().sendMavPacket(msg.pack());
//        GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "ssw", "ssw sendGimbalAxisYaw : " + yaw);
    }

    public static void sendGimbalAxisPitch(Flight flight, float pitch) {
        if(flight == null)
            return;
        msg_gimbal_control msg = new msg_gimbal_control();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();

        msg.demanded_rate_y = pitch;
        flight.getMavClient().sendMavPacket(msg.pack());
//        GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "ssw", "ssw sendGimbalAxisPitch : " + pitch);
    }

    public static void sendGimbalAxisRoll(Flight flight, float roll) {
        if(flight == null)
            return;
        msg_gimbal_control msg = new msg_gimbal_control();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();

        msg.demanded_rate_z = roll;
        flight.getMavClient().sendMavPacket(msg.pack());
//        GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "ssw", "ssw sendGimbalAxisRoll : " + roll);
    }
}
