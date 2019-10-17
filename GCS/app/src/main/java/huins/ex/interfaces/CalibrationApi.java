package huins.ex.interfaces;

import android.os.Bundle;

import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;
import huins.ex.proto.gcs.action.CalibrationActions;

/**
 * Created by suhak on 15. 10. 1.
 */
public class CalibrationApi { 

    /**
     * Start the imu calibration.
     */
    public static void startIMUCalibration(Flight flight) {
        flight.performAsyncAction(new Action(CalibrationActions.ACTION_START_IMU_CALIBRATION));
    }

    /**
     * Generate an action to send an imu calibration acknowledgement.
     */
    public static void sendIMUAck(Flight flight, int step) {
        Bundle params = new Bundle();
        params.putInt(CalibrationActions.EXTRA_IMU_STEP, step);
        flight.performAsyncAction(new Action(CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK, params));
    }

    /**
     * Start the magnetometer calibration process.
     */
    public static void startMagnetometerCalibration(Flight flight) {
        startMagnetometerCalibration(flight, false, true, 0);
    }

    /**
     * Start the magnetometer calibration process
     * @param flight vehicle to calibrate
     * @param retryOnFailure if true, automatically retry the magnetometer calibration if it fails
     * @param saveAutomatically if true, save the calibration automatically without user input.
     * @param startDelay positive delay in seconds before starting the calibration
     */
    public static void startMagnetometerCalibration(Flight flight, boolean retryOnFailure, boolean saveAutomatically,
                                                    int startDelay){
        Bundle params = new Bundle();
        params.putBoolean(CalibrationActions.EXTRA_RETRY_ON_FAILURE, retryOnFailure);
        params.putBoolean(CalibrationActions.EXTRA_SAVE_AUTOMATICALLY, saveAutomatically);
        params.putInt(CalibrationActions.EXTRA_START_DELAY, startDelay);

        flight.performAsyncAction(new Action(CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION, params));
    }

    public static void acceptMagnetometerCalibration(Flight flight) {
        flight.performAsyncAction(new Action(CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION));
    }

    /**
     * Cancel the magnetometer calibration is one if running.
     */
    public static void cancelMagnetometerCalibration(Flight flight) {
        flight.performAsyncAction(new Action(CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION));
    }
}
