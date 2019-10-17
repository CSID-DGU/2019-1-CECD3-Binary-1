package huins.ex.proto.property;

import android.os.Parcel;
import android.os.Parcelable;

import huins.ex.proto.CoordinateExtend;

/**
 * Created by fhuya on 11/5/14.
 */
public class GuidedState implements Parcelable {

    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_IDLE = 1;
    public static final int STATE_ACTIVE = 2;

    private int state;
    private CoordinateExtend coordinate;

    public GuidedState(){}

    public GuidedState(int state, CoordinateExtend coordinate) {
        this.state = state;
        this.coordinate = coordinate;
    }

    public boolean isActive(){
        return state == STATE_ACTIVE;
    }

    public boolean isIdle(){
        return state == STATE_IDLE;
    }

    public boolean isInitialized(){
        return state != STATE_UNINITIALIZED;
    }

    public CoordinateExtend getCoordinate(){
        return coordinate;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setCoordinate(CoordinateExtend coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
        dest.writeParcelable(this.coordinate, flags);
    }

    private GuidedState(Parcel in) {
        this.state = in.readInt();
        this.coordinate = in.readParcelable(CoordinateExtend.class.getClassLoader());
    }

    public static final Creator<GuidedState> CREATOR = new Creator<GuidedState>() {
        public GuidedState createFromParcel(Parcel source) {
            return new GuidedState(source);
        }

        public GuidedState[] newArray(int size) {
            return new GuidedState[size];
        }
    };
}
