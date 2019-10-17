package huins.ex.core.Flight.variables.calibration;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import huins.ex.com.MAVLink.Messages.MAVLinkMessage;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import huins.ex.com.MAVLink.ardupilotmega.msg_mag_cal_report;
import huins.ex.core.Flight.FlightInterfaces;
import huins.ex.core.Flight.FlightVariable;
import huins.ex.core.MAVLink.MavLinkCalibration;
import huins.ex.core.model.Flight;

/**
 * Created by Fredia Huya-Kouadio on 5/3/15.
 */
public class MagnetometerCalibrationImpl extends FlightVariable implements FlightInterfaces.OnFlightListener {

    public interface OnMagnetometerCalibrationListener {
        void onCalibrationCancelled();

        void onCalibrationProgress(msg_mag_cal_progress progress);

        void onCalibrationCompleted(msg_mag_cal_report result);
    }

    private final HashMap<Byte, Info> magCalibrationTracker = new HashMap<>();

    private OnMagnetometerCalibrationListener listener;

    private AtomicBoolean cancelled = new AtomicBoolean(false);

    public MagnetometerCalibrationImpl(Flight mflight) {
        super(mflight);
        mflight.addFlightListener(this);
    }

    public void setListener(OnMagnetometerCalibrationListener listener) {
        this.listener = listener;
    }

    public void startCalibration(boolean retryOnFailure, boolean saveAutomatically, int startDelay) {
        magCalibrationTracker.clear();
        cancelled.set(false);
        MavLinkCalibration.startMagnetometerCalibration(mflight, retryOnFailure, saveAutomatically, startDelay);
    }

    public void cancelCalibration() {
        MavLinkCalibration.cancelMagnetometerCalibration(mflight);

        cancelled.set(true);

        if (listener != null)
            listener.onCalibrationCancelled();
    }

    public void acceptCalibration() {
        MavLinkCalibration.acceptMagnetometerCalibration(mflight);
    }

    public void processCalibrationMessage(MAVLinkMessage message) {
        switch (message.msgid) {
            case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS: {
                msg_mag_cal_progress progress = (msg_mag_cal_progress) message;
                Info info = magCalibrationTracker.get(progress.compass_id);
                if (info == null) {
                    info = new Info();
                    magCalibrationTracker.put(progress.compass_id, info);
                }

                info.calProgress = progress;

                if (listener != null)
                    listener.onCalibrationProgress(progress);
                break;
            }

            case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT: {
                msg_mag_cal_report report = (msg_mag_cal_report) message;
                Info info = magCalibrationTracker.get(report.compass_id);
                if (info == null) {
                    info = new Info();
                    magCalibrationTracker.put(report.compass_id, info);
                }

                info.calReport = report;

                if (listener != null)
                    listener.onCalibrationCompleted((msg_mag_cal_report) message);
                break;
            }
        }
    }

    public HashMap<Byte, Info> getMagCalibrationTracker() {
        return magCalibrationTracker;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public static class Info {
        msg_mag_cal_progress calProgress;
        msg_mag_cal_report calReport;

        public msg_mag_cal_progress getCalProgress() {
            return calProgress;
        }

        public msg_mag_cal_report getCalReport() {
            return calReport;
        }
    }

    @Override
    public void onFlightEvent(FlightInterfaces.FlightEventsType event, Flight flight) {
        switch(event){
            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                cancelCalibration();
                break;
        }
    }
}
