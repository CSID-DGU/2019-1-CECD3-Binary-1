package huins.ex.proto;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by suhak on 15. 6. 23.
 */
public class Coordinate implements Parcelable, Serializable {

    private static final long serialVersionUID = -5809863197722412339L;

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void set(Coordinate update) {
        this.latitude = update.latitude;
        this.longitude = update.longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static final Creator<Coordinate> CREATOR = new Creator() {
        public Coordinate createFromParcel(Parcel source) {
            return (Coordinate) source.readSerializable();
        }

        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };

    //creator
    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //deep copy
    public Coordinate(Coordinate copy) {
        this(copy.getLatitude(), copy.getLongitude());
    }


    //maths
    public Coordinate dot(double scalar) {
        return new Coordinate(this.latitude * scalar, this.longitude * scalar);
    }

    public Coordinate negate() {
        return new Coordinate(this.latitude * -1.0D, this.longitude * -1.0D);
    }

    public Coordinate subtract(Coordinate coord) {
        return new Coordinate(this.latitude - coord.latitude, this.longitude - coord.longitude);
    }

    public Coordinate sum(Coordinate coord) {
        return new Coordinate(this.latitude + coord.latitude, this.longitude + coord.longitude);
    }

    public static Coordinate sum(Coordinate... toBeAdded) {
        double latitude = 0.0D;
        double longitude = 0.0D;
        Coordinate[] arr$ = toBeAdded;
        int len$ = toBeAdded.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Coordinate coord = arr$[i$];
            latitude += coord.latitude;
            longitude += coord.longitude;
        }

        return new Coordinate(latitude, longitude);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Coordinate)) {
            return false;
        } else {
            Coordinate latLong = (Coordinate) o;
            return Double.compare(latLong.latitude, this.latitude) != 0 ? false : Double.compare(latLong.longitude, this.longitude) == 0;
        }
    }

    public int hashCode() {
        long temp = Double.doubleToLongBits(this.latitude);
        int result = (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.longitude);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    public String toString() {
        return "Coordinate {latitude=" + this.latitude + ", longitude=" + this.longitude + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }
}
