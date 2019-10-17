package huins.ex.proto.property;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public enum FlightMode implements Parcelable {

    PLANE_MANUAL(0, Type.TYPE_PLANE, "Manual"),
    PLANE_CIRCLE(1, Type.TYPE_PLANE, "Circle"),
    PLANE_STABILIZE(2, Type.TYPE_PLANE, "Stabilize"),
    PLANE_TRAINING(3, Type.TYPE_PLANE, "Training"),
    PLANE_ACRO(4, Type.TYPE_PLANE, "Acro"),
    PLANE_FLY_BY_WIRE_A(5, Type.TYPE_PLANE, "FBW A"),
    PLANE_FLY_BY_WIRE_B(6, Type.TYPE_PLANE, "FBW B"),
    PLANE_CRUISE(7, Type.TYPE_PLANE, "Cruise"),
    PLANE_AUTOTUNE(8, Type.TYPE_PLANE, "Autotune"),
    PLANE_AUTO(10, Type.TYPE_PLANE, "Auto"),
    PLANE_RTL(11, Type.TYPE_PLANE, "RTL"),
    PLANE_LOITER(12, Type.TYPE_PLANE, "Loiter"),
    PLANE_GUIDED(15, Type.TYPE_PLANE, "Guided"),

    COPTER_STABILIZE(0, Type.TYPE_COPTER, "Stabilize"),
    COPTER_ACRO(1, Type.TYPE_COPTER, "Acro"),
    COPTER_ALT_HOLD(2, Type.TYPE_COPTER, "Alt Hold"),
    COPTER_AUTO(3, Type.TYPE_COPTER, "Auto"),
    COPTER_GUIDED(4, Type.TYPE_COPTER, "Guided"),
    COPTER_LOITER(5, Type.TYPE_COPTER, "Loiter"),
    COPTER_RTL(6, Type.TYPE_COPTER, "RTL"),
    COPTER_CIRCLE(7, Type.TYPE_COPTER, "Circle"),
    COPTER_LAND(9, Type.TYPE_COPTER, "Land"),
    COPTER_DRIFT(11, Type.TYPE_COPTER, "Drift"),
    COPTER_SPORT(13, Type.TYPE_COPTER, "Sport"),
    COPTER_FLIP(14, Type.TYPE_COPTER, "Flip"),
    COPTER_AUTOTUNE(15, Type.TYPE_COPTER, "Autotune"),
    COPTER_POSHOLD(16, Type.TYPE_COPTER, "PosHold"),
    COPTER_BRAKE(17,Type.TYPE_COPTER,"Brake"),

    ROVER_MANUAL(0, Type.TYPE_ROVER, "Manual"),
    ROVER_LEARNING(2, Type.TYPE_ROVER, "Learning"),
    ROVER_STEERING(3, Type.TYPE_ROVER, "Steering"),
    ROVER_HOLD(4, Type.TYPE_ROVER, "Hold"),
    ROVER_AUTO(10, Type.TYPE_ROVER, "Auto"),
    ROVER_RTL(11, Type.TYPE_ROVER, "RTL"),
    ROVER_GUIDED(15, Type.TYPE_ROVER, "Guided"),
    ROVER_INITIALIZING(16, Type.TYPE_ROVER, "Initializing"),

    UNKNOWN(-1, Type.TYPE_UNKNOWN, "Unknown");


    private final int mode;
    private final int droneType;
    private final String label;

    FlightMode(int mode, int droneType, String label){
        this.mode = mode;
        this.droneType = droneType;
        this.label = label;
    }

    public int getMode() {
        return mode;
    }

    public int getDroneType() {
        return droneType;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString(){
        return getLabel();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags){
        dest.writeString(name());
    }

    public static final Creator<FlightMode> CREATOR = new Creator<FlightMode>() {
        @Override
        public FlightMode createFromParcel(Parcel source) {
            return FlightMode.valueOf(source.readString());
        }

        @Override
        public FlightMode[] newArray(int size) {
            return new FlightMode[size];
        }
    };

    public static List<FlightMode> getVehicleModePerDroneType(int droneType){
        FlightMode[] availableModes = FlightMode.values();
        final List<FlightMode> flightModes = new ArrayList<FlightMode>(availableModes.length);

        for(FlightMode flightMode : availableModes){
            if(flightMode.getDroneType() == droneType)
                flightModes.add(flightMode);
        }

        return flightModes;
    }
}

