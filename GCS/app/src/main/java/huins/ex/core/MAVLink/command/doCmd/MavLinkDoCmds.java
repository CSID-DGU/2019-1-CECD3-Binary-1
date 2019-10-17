package huins.ex.core.MAVLink.command.doCmd;

import huins.ex.com.MAVLink.ardupilotmega.msg_digicam_control;
import huins.ex.com.MAVLink.common.msg_command_long;
import huins.ex.com.MAVLink.enums.MAV_CMD;
import huins.ex.core.helpers.coordinates.Coord3D;
import huins.ex.core.mission.commands.EpmGripper;
import huins.ex.core.model.Flight;

public class MavLinkDoCmds {
    public static void setROI(Flight flight, Coord3D coord) {
        if (flight == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;

        msg.param5 = (float) coord.getX();
        msg.param6 = (float) coord.getY();
        msg.param7 = (float) coord.getAltitude();

        flight.getMavClient().sendMavPacket(msg.pack());
    }

    public static void resetROI(Flight flight) {
        if (flight == null)
            return;

        setROI(flight, new Coord3D(0, 0, 0));
    }

    public static void triggerCamera(Flight flight) {
        if (flight == null)
            return;

        msg_digicam_control msg = new msg_digicam_control();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.shot = 1;
        flight.getMavClient().sendMavPacket(msg.pack());
    }

    public static void empCommand(Flight flight, boolean release) {
        if (flight == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.command = EpmGripper.MAV_CMD_DO_GRIPPER;
        msg.param2 = release ? EpmGripper.GRIPPER_ACTION_RELEASE : EpmGripper.GRIPPER_ACTION_GRAB;

        flight.getMavClient().sendMavPacket(msg.pack());
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param flight       target vehicle
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     */
    public static void setRelay(Flight flight, int relayNumber, boolean enabled) {
        if (flight == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_RELAY;
        msg.param1 = relayNumber;
        msg.param2 = enabled ? 1 : 0;

        flight.getMavClient().sendMavPacket(msg.pack());
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param flight   target vehicle
     * @param channel he output channel the servo is attached to
     * @param pwm     PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     */
    public static void setServo(Flight flight, int channel, int pwm) {
        if (flight == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = flight.getSysid();
        msg.target_component = flight.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_SERVO;
        msg.param1 = channel;
        msg.param2 = pwm;

        flight.getMavClient().sendMavPacket(msg.pack());
    }

}
