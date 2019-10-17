package huins.ex.proto.mission.item.complex;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;

import huins.ex.proto.Coordinate;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.util.Math.MathUtils;

/**
 */
public class Survey extends MissionItem implements MissionItem.ComplexItem<Survey>, android.os.Parcelable {

    private SurveyDetail surveyDetail = new SurveyDetail();
    {
        surveyDetail.setAltitude(50);
        surveyDetail.setAngle(0);
        surveyDetail.setOverlap(50);
        surveyDetail.setSidelap(60);
    }

    private double polygonArea;
    private List<Coordinate> polygonPoints = new ArrayList<Coordinate>();
    private List<Coordinate> gridPoints = new ArrayList<Coordinate>();
    private List<Coordinate> cameraLocations = new ArrayList<Coordinate>();
    private boolean isValid;

    public Survey(){
        super(MissionItemType.SURVEY);
    }

    public Survey(Survey copy){
        this();
        copy(copy);
    }

    public void copy(Survey source){
        this.surveyDetail = new SurveyDetail(source.surveyDetail);
        this.polygonArea = source.polygonArea;
        this.polygonPoints = copyPointsList(source.polygonPoints);
        this.gridPoints = copyPointsList(source.gridPoints);
        this.cameraLocations = copyPointsList(source.cameraLocations);
        this.isValid = source.isValid;
    }

    private List<Coordinate> copyPointsList(List<Coordinate> copy){
        final List<Coordinate> dest = new ArrayList<>();
        for(Coordinate itemCopy : copy){
            dest.add(new Coordinate(itemCopy));
        }

        return dest;
    }

    public SurveyDetail getSurveyDetail() {
        return surveyDetail;
    }

    public void setSurveyDetail(SurveyDetail surveyDetail) {
        this.surveyDetail = surveyDetail;
    }

    public double getPolygonArea() {
        return polygonArea;
    }

    public void setPolygonArea(double polygonArea) {
        this.polygonArea = polygonArea;
    }

    public List<Coordinate> getPolygonPoints() {
        return polygonPoints;
    }

    public void setPolygonPoints(List<Coordinate> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public List<Coordinate> getGridPoints() {
        return gridPoints;
    }

    public void setGridPoints(List<Coordinate> gridPoints) {
        this.gridPoints = gridPoints;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public double getGridLength() {
        return MathUtils.getPolylineLength(gridPoints);
    }

    public int getNumberOfLines() {
        return gridPoints.size() / 2;
    }

    public List<Coordinate> getCameraLocations() {
        return cameraLocations;
    }

    public void setCameraLocations(List<Coordinate> cameraLocations) {
        this.cameraLocations = cameraLocations;
    }

    public int getCameraCount() {
        return getCameraLocations().size();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.surveyDetail, 0);
        dest.writeDouble(this.polygonArea);
        dest.writeTypedList(polygonPoints);
        dest.writeTypedList(gridPoints);
        dest.writeTypedList(cameraLocations);
        dest.writeByte(isValid ? (byte) 1 : (byte) 0);
    }

    private Survey(Parcel in) {
        super(in);
        this.surveyDetail = in.readParcelable(SurveyDetail.class.getClassLoader());
        this.polygonArea = in.readDouble();
        in.readTypedList(polygonPoints, Coordinate.CREATOR);
        in.readTypedList(gridPoints, Coordinate.CREATOR);
        in.readTypedList(cameraLocations, Coordinate.CREATOR);
        this.isValid = in.readByte() != 0;
    }

    @Override
    public MissionItem clone() {
        return new Survey(this);
    }

    public static final Creator<Survey> CREATOR = new Creator<Survey>() {
        public Survey createFromParcel(Parcel source) {
            return new Survey(source);
        }

        public Survey[] newArray(int size) {
            return new Survey[size];
        }
    };
}
