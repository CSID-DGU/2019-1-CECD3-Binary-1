package huins.ex.proto.mission.item.spatial;

import android.os.Parcel;

import huins.ex.proto.CoordinateExtend;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;

/**
 * Created by fhuya on 11/6/14.
 */
public abstract class BaseSpatialItem extends MissionItem implements MissionItem.SpatialItem, android.os.Parcelable {

    private CoordinateExtend coordinate;

    protected BaseSpatialItem(MissionItemType type) {
        super(type);
    }

    protected BaseSpatialItem(BaseSpatialItem copy){
        this(copy.getType());
        coordinate = copy.coordinate == null ? null : new CoordinateExtend(copy.coordinate);
    }

    @Override
    public CoordinateExtend getCoordinate() {
        return coordinate;
    }

    @Override
    public void setCoordinate(CoordinateExtend coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.coordinate, flags);
    }

    protected BaseSpatialItem(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(CoordinateExtend.class.getClassLoader());
    }
}
