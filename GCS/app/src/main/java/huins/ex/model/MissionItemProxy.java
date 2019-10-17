package huins.ex.model;

import java.util.ArrayList;
import java.util.List;

import huins.ex.interfaces.MarkerInfo;
import huins.ex.model.markers.MissionItemMarkerInfo;
import huins.ex.proto.Coordinate;
import huins.ex.proto.Flight;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.complex.StructureScanner;
import huins.ex.proto.mission.item.complex.Survey;
import huins.ex.proto.mission.item.spatial.Circle;
import huins.ex.util.Math.MathUtils;
import huins.ex.view.fragments.missions.MissionDetailFragment;

/**
 * Created by suhak on 15. 6. 30.
 */
public class MissionItemProxy {

    private final Flight.OnMissionItemsBuiltCallback missionItemBuiltListener = new Flight.OnMissionItemsBuiltCallback() {
        @Override
        public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
            mMission.notifyMissionUpdate(false);
        }
    };

    /**
     * This is the mission item object this class is built around.
     */
    private final MissionItem mMissionItem;

    /**
     * This is the mission render to which this item belongs.
     */
    private final MissionProxy mMission;

    /**
     * This is the marker source for this mission item render.
     */
    private final List<MarkerInfo> mMarkerInfos;

    /**
     * Used by the mission item list adapter to provide drag and drop support.
     */
    private final long stableId;

    public MissionItemProxy(MissionProxy mission, MissionItem missionItem) {
        this.stableId = System.nanoTime();

        mMission = mission;
        mMissionItem = missionItem;
        mMarkerInfos = MissionItemMarkerInfo.newInstance(this);

        if(mMissionItem instanceof Survey){
            mMission.getFlight().buildMissionItemsAsync(missionItemBuiltListener, (Survey) mMissionItem);
        }
        else if(mMissionItem instanceof StructureScanner){
            mMission.getFlight().buildMissionItemsAsync(missionItemBuiltListener, (StructureScanner) mMissionItem);
        }
    }

    public MissionProxy getMissionProxy() {
        return mMission;
    }

    public MissionProxy getMission(){return mMission;}

    public MissionItem getMissionItem() {
        return mMissionItem;
    }

    public MissionDetailFragment getDetailFragment() {
        return MissionDetailFragment.newInstance(mMissionItem.getType());
    }

    public List<MarkerInfo> getMarkerInfos() {
        return mMarkerInfos;
    }

    public List<Coordinate> getPath(Coordinate previousPoint) {
        List<Coordinate> pathPoints = new ArrayList<Coordinate>();
        switch (mMissionItem.getType()) {
            case LAND:
            case WAYPOINT:
            case SPLINE_WAYPOINT:
                pathPoints.add(((MissionItem.SpatialItem) mMissionItem).getCoordinate());
                break;

            case CIRCLE:
                for (int i = 0; i <= 360; i += 10) {
                    Circle circle = (Circle) mMissionItem;
                    double startHeading = 0;
                    if (previousPoint != null) {
                        startHeading = MathUtils.getHeadingFromCoordinates(circle.getCoordinate(),
                                previousPoint);
                    }
                    pathPoints.add(MathUtils.newCoordFromBearingAndDistance(circle.getCoordinate(),
                            startHeading + i, circle.getRadius()));
                }
                break;

            case SURVEY:
                List<Coordinate> gridPoints = ((Survey)mMissionItem).getGridPoints();
                if (gridPoints != null && !gridPoints.isEmpty()) {
                    pathPoints.addAll(gridPoints);
                }
                break;

            case STRUCTURE_SCANNER:
                StructureScanner survey = (StructureScanner)mMissionItem;
                pathPoints.addAll(survey.getPath());
                break;

            default:
                break;
        }

        return pathPoints;
    }

    /**
     * @return stable id used by the recycler view adapter to provide drag and drop support.
     */
    public long getStableId(){
        return stableId;
    }
}
