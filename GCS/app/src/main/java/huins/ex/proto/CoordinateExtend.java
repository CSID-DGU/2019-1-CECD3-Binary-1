package huins.ex.proto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by suhak on 15. 6. 26.
 */
public class CoordinateExtend extends Coordinate {

    private static final long serialVersionUID = -4771550293045623743L;

    /**
     * Stores the altitude in meters.
     */
    private double mAltitude;

    public CoordinateExtend(double latitude, double longitude, double altitude) {
        super(latitude, longitude);
        mAltitude = altitude;
    }

    public CoordinateExtend(Coordinate location, double altitude) {
        super(location);
        mAltitude = altitude;
    }

    public CoordinateExtend(CoordinateExtend copy) {
        this(copy.getLatitude(), copy.getLongitude(), copy.getAltitude());
    }

    public void set(CoordinateExtend source) {
        super.set(source);
        this.mAltitude = source.mAltitude;
    }

    /**
     * @return the altitude in meters
     */
    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        this.mAltitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoordinateExtend)) return false;
        if (!super.equals(o)) return false;

        CoordinateExtend that = (CoordinateExtend) o;

        if (Double.compare(that.mAltitude, mAltitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(mAltitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final String superToString = super.toString();
        return "LatLongAlt{" +
                superToString +
                ", mAltitude=" + mAltitude +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Parcelable.Creator<CoordinateExtend> CREATOR = new Parcelable.Creator<CoordinateExtend>() {
        public CoordinateExtend createFromParcel(Parcel source) {
            return (CoordinateExtend) source.readSerializable();
        }

        public CoordinateExtend[] newArray(int size) {
            return new CoordinateExtend[size];
        }
    };
}