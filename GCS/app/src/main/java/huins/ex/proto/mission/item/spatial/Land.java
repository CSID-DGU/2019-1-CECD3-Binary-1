package huins.ex.proto.mission.item.spatial;

import android.os.Parcel;

import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;

/**
 * Created by fhuya on 11/6/14.
 */
public class Land extends BaseSpatialItem implements android.os.Parcelable {

    public Land(){
        super(MissionItemType.LAND);
    }

    public Land(Land copy){
        super(copy);
    }

    private Land(Parcel in) {
        super(in);
    }

    @Override
    public MissionItem clone() {
        return new Land(this);
    }

    public static final Creator<Land> CREATOR = new Creator<Land>() {
        public Land createFromParcel(Parcel source) {
            return new Land(source);
        }

        public Land[] newArray(int size) {
            return new Land[size];
        }
    };
}
