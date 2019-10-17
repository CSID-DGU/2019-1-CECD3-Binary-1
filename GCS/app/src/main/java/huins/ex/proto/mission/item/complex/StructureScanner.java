package huins.ex.proto.mission.item.complex;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import huins.ex.proto.Coordinate;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.spatial.BaseSpatialItem;

/**
 *
 */
public class StructureScanner extends BaseSpatialItem implements MissionItem.ComplexItem<StructureScanner>, Parcelable {

    private double radius = 10;
    private double heightStep = 5;
    private int stepsCount = 2;
    private boolean crossHatch = false;
    private SurveyDetail surveyDetail = new SurveyDetail();
    private List<Coordinate> path = new ArrayList<Coordinate>();

    public StructureScanner(){
        super(MissionItemType.STRUCTURE_SCANNER);
    }

    public StructureScanner(StructureScanner copy){
        super(copy);
        copy(copy);
    }

    public void copy(StructureScanner source){
        this.radius = source.radius;
        this.heightStep = source.heightStep;
        this.stepsCount = source.stepsCount;
        this.crossHatch = source.crossHatch;
        this.surveyDetail = new SurveyDetail(source.surveyDetail);
        this.path = copyPointsList(source.path);
    }

    private List<Coordinate> copyPointsList(List<Coordinate> copy){
        final List<Coordinate> dest = new ArrayList<>();
        for(Coordinate itemCopy : copy){
            dest.add(new Coordinate(itemCopy));
        }

        return dest;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getHeightStep() {
        return heightStep;
    }

    public void setHeightStep(double heightStep) {
        this.heightStep = heightStep;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    public boolean isCrossHatch() {
        return crossHatch;
    }

    public void setCrossHatch(boolean crossHatch) {
        this.crossHatch = crossHatch;
    }

    public SurveyDetail getSurveyDetail() {
        return surveyDetail;
    }

    public void setSurveyDetail(SurveyDetail surveyDetail) {
        this.surveyDetail = surveyDetail;
    }

    public List<Coordinate> getPath() {
        return path;
    }

    public void setPath(List<Coordinate> points){
        this.path = points;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.radius);
        dest.writeDouble(this.heightStep);
        dest.writeInt(this.stepsCount);
        dest.writeByte(crossHatch ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.surveyDetail, 0);
        dest.writeTypedList(path);
    }

    private StructureScanner(Parcel in) {
        super(in);
        this.radius = in.readDouble();
        this.heightStep = in.readDouble();
        this.stepsCount = in.readInt();
        this.crossHatch = in.readByte() != 0;
        this.surveyDetail = in.readParcelable(SurveyDetail.class.getClassLoader());
        in.readTypedList(path, Coordinate.CREATOR);
    }

    @Override
    public MissionItem clone() {
        return new StructureScanner(this);
    }

    public static final Creator<StructureScanner> CREATOR = new Creator<StructureScanner>() {
        public StructureScanner createFromParcel(Parcel source) {
            return new StructureScanner(source);
        }

        public StructureScanner[] newArray(int size) {
            return new StructureScanner[size];
        }
    };
}
