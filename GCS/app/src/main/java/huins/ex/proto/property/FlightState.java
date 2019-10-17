package huins.ex.proto.property;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by suhak on 15. 6. 26.
 */
public class FlightState implements Parcelable {

    private boolean isConnected;
    private boolean armed;
    private boolean isFlying;
    private String calibrationStatus;
    private String autopilotErrorId;
    private long flightStartTime;

    private FlightMode vehicleMode = FlightMode.UNKNOWN;
    private boolean isTelemetryLive;

    public FlightState() {
    }

    public FlightState(boolean isConnected, FlightMode mode, boolean armed, boolean flying,
                 String autopilotErrorId, int mavlinkVersion, String calibrationStatus, long flightStartTime, boolean isTelemetryLive) {
        this.isConnected = isConnected;
        this.armed = armed;
        this.isFlying = flying;
        this.flightStartTime = flightStartTime;
        this.autopilotErrorId = autopilotErrorId;
        this.calibrationStatus = calibrationStatus;

        if (mode != null)
            this.vehicleMode = mode;

        this.isTelemetryLive = isTelemetryLive;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }

    public void setFlying(boolean isFlying) {
        this.isFlying = isFlying;
    }

    public void setCalibrationStatus(String calibrationStatus) {
        this.calibrationStatus = calibrationStatus;
    }

    public void setVehicleMode(FlightMode vehicleMode) {
        this.vehicleMode = vehicleMode;
    }

    public boolean isArmed() {
        return armed;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public FlightMode getVehicleMode() {
        return vehicleMode;
    }

    public String getAutopilotErrorId() {
        return autopilotErrorId;
    }

    public void setAutopilotErrorId(String autopilotErrorId) {
        this.autopilotErrorId = autopilotErrorId;
    }

    public boolean isWarning() {
        return TextUtils.isEmpty(autopilotErrorId);
    }

    public boolean isCalibrating() {
        return calibrationStatus != null;
    }

    public void setCalibration(String message) {
        this.calibrationStatus = message;
    }

    public String getCalibrationStatus() {
        return this.calibrationStatus;
    }

    public long getFlightStartTime() {
        return flightStartTime;
    }

    public void setFlightStartTime(long flightStartTime) {
        this.flightStartTime = flightStartTime;
    }

    public boolean isTelemetryLive() {
        return isTelemetryLive;
    }

    public void setIsTelemetryLive(boolean isTelemetryLive) {
        this.isTelemetryLive = isTelemetryLive;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isConnected ? (byte) 1 : (byte) 0);
        dest.writeByte(armed ? (byte) 1 : (byte) 0);
        dest.writeByte(isFlying ? (byte) 1 : (byte) 0);
        dest.writeString(this.calibrationStatus);
        dest.writeParcelable(this.vehicleMode, 0);
        dest.writeString(this.autopilotErrorId);
        dest.writeLong(this.flightStartTime);
        dest.writeByte(isTelemetryLive ? (byte) 1 : (byte) 0);
    }

    private FlightState(Parcel in) {
        this.isConnected = in.readByte() != 0;
        this.armed = in.readByte() != 0;
        this.isFlying = in.readByte() != 0;
        this.calibrationStatus = in.readString();
        this.vehicleMode = in.readParcelable(FlightMode.class.getClassLoader());
        this.autopilotErrorId = in.readString();
        this.flightStartTime = in.readLong();
        this.isTelemetryLive = in.readByte() != 0;
    }

    public static final Creator<FlightState> CREATOR = new Creator<FlightState>() {
        public FlightState createFromParcel(Parcel source) {
            return new FlightState(source);
        }

        public FlightState[] newArray(int size) {
            return new FlightState[size];
        }
    };
}
