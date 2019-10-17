package huins.ex.proto.property;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import huins.ex.proto.Coordinate;
import huins.ex.util.Math.MathUtils;

/**
 * Created by fhuya on 11/11/14.
 */
public class FootPrint implements Parcelable {

    private double meanGSD;
    private List<Coordinate> vertex = new ArrayList<Coordinate>();

    public FootPrint(){}

    public FootPrint(double meanGSD, List<Coordinate> vertex) {
        this.meanGSD = meanGSD;
        this.vertex = vertex;
    }

    public void setMeanGSD(double meanGSD) {
        this.meanGSD = meanGSD;
    }

    public void setVertex(List<Coordinate> vertex) {
        this.vertex = vertex;
    }

    public double getMeanGSD() {
        return meanGSD;
    }

    public List<Coordinate> getVertexInGlobalFrame() {
        return vertex;
    }

    public double getLateralSize() {
        return  (MathUtils.getDistance2D(vertex.get(0), vertex.get(1))
                + MathUtils.getDistance2D(vertex.get(2), vertex.get(3))) / 2;
    }

    public double getLongitudinalSize() {
        return (MathUtils.getDistance2D(vertex.get(0), vertex.get(3))
                + MathUtils.getDistance2D(vertex.get(1), vertex.get(2))) / 2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.meanGSD);
        dest.writeTypedList(vertex);
    }

    private FootPrint(Parcel in) {
        this.meanGSD = in.readDouble();
        in.readTypedList(vertex, Coordinate.CREATOR);
    }

    public static final Creator<FootPrint> CREATOR = new Creator<FootPrint>() {
        public FootPrint createFromParcel(Parcel source) {
            return new FootPrint(source);
        }

        public FootPrint[] newArray(int size) {
            return new FootPrint[size];
        }
    };
}
