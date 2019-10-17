package huins.ex.model.markers;

import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.Coordinate;
import huins.ex.proto.mission.item.complex.Survey;

/**
 */
public class PolygonMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

	private Coordinate mPoint;
    private final Survey survey;
    private final int polygonIndex;

	public PolygonMarkerInfo(Coordinate point, Survey mSurvey, int index) {
		mPoint = point;
		survey = mSurvey;
		polygonIndex = index;
	}

	public Survey getSurvey(){
		return survey;
	}

	public int getIndex(){
		return polygonIndex;
	}

	
	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Coordinate getPosition() {
		return mPoint;
	}

	@Override
	public void setPosition(Coordinate coord) {
		mPoint = coord;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}
}
