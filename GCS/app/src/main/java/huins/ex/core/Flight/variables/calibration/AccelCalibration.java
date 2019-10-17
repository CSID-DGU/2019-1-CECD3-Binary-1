package huins.ex.core.Flight.variables.calibration;

import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.common.msg_statustext;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightInterfaces.FlightEventsType;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkCalibration;
import huins.ex.core.model.Flight;

public class AccelCalibration extends FlightVariable implements FlightInterfaces.OnFlightListener {
    private String mavMsg;
    private boolean calibrating;

    public AccelCalibration(Flight drone) {
        super(drone);
        drone.addFlightListener(this);
    }

    public boolean startCalibration() {
        if(calibrating)
            return true;

        if (mflight.getState().isFlying()) {
            calibrating = false;
        } else {
            calibrating = true;
            mavMsg = "";
            MavLinkCalibration.startAccelerometerCalibration(mflight);
        }
        return calibrating;
    }

    public void sendAck(int step) {
        if(calibrating)
            MavLinkCalibration.sendCalibrationAckMessage(step, mflight);
    }

    public void processMessage(MAVLinkMessage msg) {
        if (calibrating && msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
            msg_statustext statusMsg = (msg_statustext) msg;
            final String message = statusMsg.getText();

            if (message != null && (message.startsWith("Place vehicle") || message.startsWith("Calibration"))) {
                mavMsg = message;
                if (message.startsWith("Calibration"))
                    calibrating = false;

                mflight.notifyFlightEvent(FlightEventsType.CALIBRATION_IMU);
            }
        }
    }

    public String getMessage() {
        return mavMsg;
    }

    public boolean isCalibrating() {
        return calibrating;
    }

    @Override
    public void onFlightEvent(FlightEventsType event, Flight flight) {
        switch (event) {
            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                if (calibrating)
                    cancelCalibration();
                break;
        }
    }

    public void cancelCalibration() {
        mavMsg = "";
        calibrating = false;
    }
}
