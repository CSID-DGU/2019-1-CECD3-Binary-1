package huins.ex.model.markers;

import java.util.ArrayList;
import java.util.List;

import huins.ex.interfaces.MarkerInfo;
import huins.ex.model.MissionItemProxy;
import huins.ex.proto.Coordinate;
import huins.ex.proto.mission.item.complex.Survey;

/**
 *
 */
public class SurveyMarkerInfoProvider {

	private final Survey mSurvey;
	private final List<MarkerInfo> mPolygonMarkers = new ArrayList<MarkerInfo>();

	protected SurveyMarkerInfoProvider(MissionItemProxy origin) {
		mSurvey = (Survey) origin.getMissionItem();
		updateMarkerInfoList();
	}

	private void updateMarkerInfoList() {
        List<Coordinate> points = mSurvey.getPolygonPoints();
        if(points != null) {
            final int pointsCount = points.size();
            for (int i = 0; i < pointsCount; i++) {
                mPolygonMarkers.add(new PolygonMarkerInfo(points.get(i), mSurvey, i));
            }
        }
	}

	public List<MarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}
}
