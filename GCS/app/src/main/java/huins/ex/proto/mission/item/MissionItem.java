package huins.ex.proto.mission.item;

import android.os.Parcel;
import android.os.Parcelable;

import huins.ex.proto.CoordinateExtend;
import huins.ex.proto.mission.MissionItemType;

/**
 * Created by fhuya on 11/5/14.
 */
public abstract class MissionItem implements Cloneable, Parcelable {

    public interface Command {}

    public interface SpatialItem {
        CoordinateExtend getCoordinate();

        void setCoordinate(CoordinateExtend coordinate);
    }

    public interface ComplexItem<T extends MissionItem> {
        void copy(T source);
    }

    private final MissionItemType type;

    protected MissionItem(MissionItemType type) {
        this.type = type;
    }

    public MissionItemType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type.ordinal());
    }

    protected MissionItem(Parcel in){
        this.type = MissionItemType.values()[in.readInt()];
    }

    @Override
    public abstract MissionItem clone();

}
