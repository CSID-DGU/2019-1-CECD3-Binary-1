package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.ardupilotmega.msg_digicam_control;
import huins.ex.core.model.Flight;

/**
 * Created by ssw on 2016-06-03.
 */
public class MavLinkCameraOn {
    public static void sendCameraOn(Flight flight, boolean is_on) {
        if(flight == null)
            return;
        msg_digicam_control msg = new msg_digicam_control();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();

        if(is_on)
            msg.session = 1;
        else
            msg.session = 0;

        flight.getMavClient().sendMavPacket(msg.pack());

//        if (flight == null)
//            return;
//
//        msg_digicam_control msg = new msg_digicam_control();
//        msg.target_system = flight.getSysid();
//        msg.target_component = flight.getCompid();
//        msg.shot = 1;
//        flight.getMavClient().sendMavPacket(msg.pack());
    }
}
