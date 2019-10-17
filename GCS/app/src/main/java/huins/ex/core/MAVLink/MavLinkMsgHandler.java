package huins.ex.core.MAVLink;

import huins.ex.com.MAVLink.Messages.ApmModes;
import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.ardupilotmega.msg_camera_feedback;
import huins.ex.com.MAVLink.ardupilotmega.msg_ekf_status_report;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_get_response;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_heartbeat;
import huins.ex.com.MAVLink.ardupilotmega.msg_gopro_set_response;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_report;
import huins.ex.com.MAVLink.ardupilotmega.msg_mount_status;
import huins.ex.com.MAVLink.ardupilotmega.msg_radio;
import huins.ex.com.MAVLink.common.msg_attitude;
import huins.ex.com.MAVLink.common.msg_command_ack;
import huins.ex.com.MAVLink.common.msg_global_position_int;
import huins.ex.com.MAVLink.common.msg_gps_raw_int;
import huins.ex.com.MAVLink.common.msg_heartbeat;
import huins.ex.com.MAVLink.common.msg_mission_current;
import huins.ex.com.MAVLink.common.msg_mission_item;
import huins.ex.com.MAVLink.common.msg_named_value_int;
import huins.ex.com.MAVLink.common.msg_nav_controller_output;
import huins.ex.com.MAVLink.common.msg_radio_status;
import huins.ex.com.MAVLink.common.msg_raw_imu;
import huins.ex.com.MAVLink.common.msg_rc_channels_raw;
import huins.ex.com.MAVLink.common.msg_servo_output_raw;
import huins.ex.com.MAVLink.common.msg_statustext;
import huins.ex.com.MAVLink.common.msg_sys_status;
import huins.ex.com.MAVLink.common.msg_vfr_hud;
import huins.ex.com.MAVLink.enums.MAV_MODE_FLAG;
import huins.ex.com.MAVLink.enums.MAV_SEVERITY;
import huins.ex.com.MAVLink.enums.MAV_STATE;
import huins.ex.com.MAVLink.enums.MAV_SYS_STATUS_SENSOR;
import huins.ex.core.Flight.variables.Home;
import huins.ex.core.model.Flight;

/**
 * Parse the received mavlink messages, and update the flight state appropriately.
 */
public class MavLinkMsgHandler {

    private Flight flight;

    public MavLinkMsgHandler(Flight flight) {
        this.flight = flight;
    }

    public void receiveData(MAVLinkMessage msg) {
        if (flight.getParameters().processMessage(msg)) {
            return;
        }

        flight.getWaypointManager().processMessage(msg);
        flight.getCalibrationSetup().processMessage(msg);

        switch (msg.msgid) {
            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                msg_attitude m_att = (msg_attitude) msg;
                flight.getOrientation().setRollPitchYaw(m_att.roll * 180.0 / Math.PI,
                        m_att.pitch * 180.0 / Math.PI, m_att.yaw * 180.0 / Math.PI);
                break;

            case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
                msg_vfr_hud m_hud = (msg_vfr_hud) msg;
                flight.setAltitudeGroundAndAirSpeeds(m_hud.alt, m_hud.groundspeed, m_hud.airspeed, m_hud.climb);
                break;

            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
                flight.getMissionStats().setWpno(((msg_mission_current) msg).seq);
                break;

            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
                msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
                flight.setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error);
                flight.getNavigation().setNavPitchRollYaw(m_nav.nav_pitch, m_nav.nav_roll, m_nav.nav_bearing);
                break;

            case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
                msg_raw_imu msg_imu = (msg_raw_imu) msg;
                flight.getMagnetometer().newData(msg_imu);
                break;

            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) msg;
                flight.setType(msg_heart.type);
                checkIfFlying(msg_heart);
                processState(msg_heart);
                ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, flight.getType());
                flight.getState().setMode(newMode);
                flight.onHeartbeat(msg_heart);
                break;

            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                flight.getGps().setPosition(((msg_global_position_int) msg).lat / 1E7,
                                ((msg_global_position_int) msg).lon / 1E7);
                break;

            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                msg_sys_status m_sys = (msg_sys_status) msg;
                flight.getBattery().setBatteryState(m_sys.voltage_battery / 1000.0,
                        m_sys.battery_remaining, m_sys.current_battery / 100.0);
                checkControlSensorsHealth(m_sys);
                break;

            case msg_radio.MAVLINK_MSG_ID_RADIO:
                msg_radio m_radio = (msg_radio) msg;
                flight.getRadio().setRadioState(m_radio.rxerrors, m_radio.fixed, m_radio.rssi,
                        m_radio.remrssi, m_radio.txbuf, m_radio.noise, m_radio.remnoise);
                break;

            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                msg_radio_status m_radio_status = (msg_radio_status) msg;
                flight.getRadio().setRadioState(m_radio_status.rxerrors, m_radio_status.fixed, m_radio_status.rssi,
                        m_radio_status.remrssi, m_radio_status.txbuf, m_radio_status.noise, m_radio_status.remnoise);
                break;

            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                flight.getGps().setGpsState(((msg_gps_raw_int) msg).fix_type,
                        ((msg_gps_raw_int) msg).satellites_visible, ((msg_gps_raw_int) msg).eph);
                break;

            case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
                flight.getRC().setRcInputValues((msg_rc_channels_raw) msg);
                break;

            case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
                flight.getRC().setRcOutputValues((msg_servo_output_raw) msg);
                break;

            case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                // These are any warnings sent from APM:Copter with
                // gcs_send_text_P()
                // This includes important thing like arm fails, prearm fails, low
                // battery, etc.
                // also less important things like "erasing logs" and
                // "calibrating barometer"
                msg_statustext msg_statustext = (msg_statustext) msg;
                processStatusText(msg_statustext);
                break;

            case msg_camera_feedback.MAVLINK_MSG_ID_CAMERA_FEEDBACK:
                flight.getCamera().newImageLocation((msg_camera_feedback) msg);
                break;

            case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
                flight.getCamera().updateMountOrientation(((msg_mount_status) msg));
                break;

            case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT:
                processNamedValueInt((msg_named_value_int) msg);
                break;

            //*************** GoPro messages handling **************//
            case msg_gopro_heartbeat.MAVLINK_MSG_ID_GOPRO_HEARTBEAT:
                flight.getGoProImpl().onHeartBeat((msg_gopro_heartbeat) msg);
                break;

            case msg_gopro_set_response.MAVLINK_MSG_ID_GOPRO_SET_RESPONSE:
                flight.getGoProImpl().onResponseReceived((msg_gopro_set_response) msg);
                break;

            case msg_gopro_get_response.MAVLINK_MSG_ID_GOPRO_GET_RESPONSE:
                flight.getGoProImpl().onResponseReceived((msg_gopro_get_response)msg);
                break;

            //*************** Magnetometer calibration messages handling *************//
            case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS:
            case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT:
                flight.getMagnetometerCalibration().processCalibrationMessage(msg);
                break;

            //*************** EKF State handling ******************//
            case msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS_REPORT:
                flight.getState().setEkfStatus((msg_ekf_status_report) msg);
                break;

            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
                msg_mission_item missionItem = (msg_mission_item) msg;
                if(missionItem.seq == Home.HOME_WAYPOINT_INDEX){
                    flight.getHome().setHome(missionItem);
                }
                break;

            //**************** Command long acknowledgement ******************//
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                final msg_command_ack commandAck = (msg_command_ack) msg;
                handleCommandAck(commandAck);
                break;

            default:
                break;
        }
    }

    private void handleCommandAck(msg_command_ack ack){
        if(ack != null){
            System.out.println(ack.toString());
        }
    }

    private void processNamedValueInt(msg_named_value_int message){
        if(message == null)
            return;

        switch (message.getName()) {
            case "ARMMASK":
                //Give information about the vehicle's ability to arm successfully.
                final ApmModes vehicleMode = flight.getState().getMode();
                if (ApmModes.isCopter(vehicleMode.getType())) {
                    final int value = message.value;
                    final boolean isReadyToArm = (value & (1 << vehicleMode.getNumber())) != 0;
                    final String armReadinessMsg = isReadyToArm ? "READY TO ARM" : "UNREADY FOR ARMING";
                    flight.logMessage(MAV_SEVERITY.MAV_SEVERITY_NOTICE, armReadinessMsg);
                }
                break;
        }
    }

    private void checkIfFlying(msg_heartbeat msg_heart) {
        final byte systemStatus = msg_heart.system_status;
        final boolean wasFlying = flight.getState().isFlying();

        final boolean isFlying = systemStatus == MAV_STATE.MAV_STATE_ACTIVE
                || (wasFlying
                && (systemStatus == MAV_STATE.MAV_STATE_CRITICAL || systemStatus == MAV_STATE.MAV_STATE_EMERGENCY));

        flight.getState().setIsFlying(isFlying);
    }

    private void processState(msg_heartbeat msg_heart) {
        checkArmState(msg_heart);
        checkFailsafe(msg_heart);
    }

    private void checkFailsafe(msg_heartbeat msg_heart) {
        boolean failsafe2 = msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_CRITICAL
                || msg_heart.system_status == MAV_STATE.MAV_STATE_EMERGENCY;

        if (failsafe2) {
            flight.getState().repeatWarning();
        }
    }

    private void checkControlSensorsHealth(msg_sys_status sysStatus){
        boolean isRCFailsafe = (sysStatus.onboard_control_sensors_health & MAV_SYS_STATUS_SENSOR
                .MAV_SYS_STATUS_SENSOR_RC_RECEIVER) == 0;
        if(isRCFailsafe){
            flight.getState().parseAutopilotError("RC FAILSAFE");
        }
    }

    private void checkArmState(msg_heartbeat msg_heart) {
        flight.getState().setArmed(
                (msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED);
    }

    private void processStatusText(msg_statustext statusText) {
        String message = statusText.getText();

        switch (message) {
            case "ArduCopter":
            case "ArduPlane":
            case "ArduRover":
                flight.setFirmwareVersion(message);
                break;

            default:
                //Try parsing as an error.
                if(!flight.getState().parseAutopilotError(message)) {
                    //Relay to the connected client.
                    flight.logMessage(statusText.severity, message);
                }
                break;
        }
    }
}
