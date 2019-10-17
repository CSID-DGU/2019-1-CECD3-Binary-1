package huins.ex.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.CircularArray;
import android.util.Pair;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.Coordinate;
import huins.ex.proto.CoordinateExtend;
import huins.ex.proto.Flight;
import huins.ex.proto.MapInterface;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.mission.Mission;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.MissionItem.SpatialItem;
import huins.ex.proto.mission.item.command.ReturnToLaunch;
import huins.ex.proto.mission.item.command.Takeoff;
import huins.ex.proto.mission.item.complex.StructureScanner;
import huins.ex.proto.mission.item.complex.Survey;
import huins.ex.proto.mission.item.complex.SurveyDetail;
import huins.ex.proto.mission.item.spatial.BaseSpatialItem;
import huins.ex.proto.mission.item.spatial.RegionOfInterest;
import huins.ex.proto.mission.item.spatial.SplineWaypoint;
import huins.ex.proto.mission.item.spatial.Waypoint;
import huins.ex.util.File.MissionWriter;
import huins.ex.util.Math.MathUtils;
import huins.ex.util.analytics.GAUtils;

/**
 * Created by suhak on 15. 6. 30.
 */
public class MissionProxy implements MapInterface.PathSource {

    public static final String ACTION_MISSION_PROXY_UPDATE = "ACTION_MISSION_PROXY_UPDATE";

    private static final double DEFAULT_ALTITUDE = 20; //meters
    private static final int UNDO_BUFFER_SIZE = 30;

    private static final IntentFilter eventFilter = new IntentFilter();

    private final CircularArray<Mission> undoBuffer = new CircularArray<>(UNDO_BUFFER_SIZE);

    private final List<MissionItemProxy> missionItemProxies = new ArrayList<MissionItemProxy>();

    private final LocalBroadcastManager lbm;
    private Flight flight;

    private Mission currentMission;
    public MissionSelection selection = new MissionSelection();


    static {
        eventFilter.addAction(AttributeEvent.MISSION_DRONIE_CREATED);
        eventFilter.addAction(AttributeEvent.MISSION_UPDATED);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
    }

    private static final String TAG = MissionProxy.class.getSimpleName();

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (AttributeEvent.MISSION_DRONIE_CREATED.equals(action)
                    || AttributeEvent.MISSION_UPDATED.equals(action)
                    || AttributeEvent.MISSION_RECEIVED.equals(action)) {
                Mission droneMission = flight.getAttribute(AttributeType.MISSION);
                load(droneMission);
            }
        }
    };

    private final Flight.OnMissionItemsBuiltCallback missionItemsBuiltListener = new Flight.OnMissionItemsBuiltCallback() {
        @Override
        public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
            notifyMissionUpdate(false);
        }
    };

    public MissionProxy(Context context, Flight flight) {
        this.flight = flight;
        this.currentMission = generateMission(true);
        lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(eventReceiver, eventFilter);
    }

    public void setFlight(Flight flight){
        this.flight = flight;
    }

    public void notifyMissionUpdate() {
        notifyMissionUpdate(true);
    }

    public boolean canUndoMission() {
        return !undoBuffer.isEmpty();
    }

    public void undoMission() {
        if (!canUndoMission())
            throw new IllegalStateException("Invalid state for mission undoing.");

        Mission previousMission = undoBuffer.popLast();
        load(previousMission, false);
    }

    public void notifyMissionUpdate(boolean saveMission) {
        if (saveMission && currentMission != null) {
            //Store the current state of the mission.
            undoBuffer.addLast(currentMission);
        }

        currentMission = generateMission(true);
        lbm.sendBroadcast(new Intent(ACTION_MISSION_PROXY_UPDATE));
    }

    public List<MissionItemProxy> getItems() {
        return missionItemProxies;
    }

    private MissionItem[] getMissionItems() {
        List<MissionItem> missionItems = new ArrayList<MissionItem>(missionItemProxies.size());
        for (MissionItemProxy mip : missionItemProxies)
            missionItems.add(mip.getMissionItem());

        return missionItems.toArray(new MissionItem[missionItems.size()]);
    }

    public Flight getFlight() {
        return this.flight;
    }
    public List<MarkerInfo> getMarkersInfos() {
        List<MarkerInfo> markerInfos = new ArrayList<MarkerInfo>();

        for (MissionItemProxy itemProxy : missionItemProxies) {
            List<MarkerInfo> itemMarkerInfos = itemProxy.getMarkerInfos();
            if (itemMarkerInfos != null && !itemMarkerInfos.isEmpty()) {
                markerInfos.addAll(itemMarkerInfos);
            }
        }
        return markerInfos;
    }

    public void load(Mission mission) {
        load(mission, true);
    }

    private void load(Mission mission, boolean isNew) {
        if (mission == null)
            return;

        if (isNew) {
            currentMission = null;
            clearUndoBuffer();
        }

        selection.mSelectedItems.clear();
        missionItemProxies.clear();

        for (MissionItem item : mission.getMissionItems()) {
            missionItemProxies.add(new MissionItemProxy(this, item));
        }

        selection.notifySelectionUpdate();

        notifyMissionUpdate(isNew);
    }

    private void clearUndoBuffer(){
        while(!undoBuffer.isEmpty())
            undoBuffer.popLast();
    }

    /**
     * Checks if this mission render contains the passed argument.
     *
     * @param item mission item render object
     * @return true if this mission render contains the passed argument
     */
    public boolean contains(MissionItemProxy item) {
        return missionItemProxies.contains(item);
    }

    /**
     * Removes a waypoint mission item from the set of mission items commands.
     *
     * @param item item to remove
     */
    public void removeItem(MissionItemProxy item) {
        missionItemProxies.remove(item);
        selection.mSelectedItems.remove(item);

        selection.notifySelectionUpdate();
        notifyMissionUpdate();
    }

    /**
     * Adds a survey mission item to the set.
     *
     * @param points 2D points making up the survey
     */
    public void addSurveyPolygon(List<Coordinate> points) {
        Survey survey = new Survey();
        survey.setPolygonPoints(points);
        addMissionItem(survey);
    }

    /**
     * Add a set of waypoints generated around the passed 2D points.
     *
     * @param points list of points used to generate the mission waypoints
     */
    public void addWaypoints(List<Coordinate> points) {
        final double alt = getLastAltitude();
        final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for (Coordinate point : points) {
            Waypoint waypoint = new Waypoint();
            waypoint.setCoordinate(new CoordinateExtend(point.getLatitude(), point.getLongitude(),
                    (float) alt));
            missionItemsToAdd.add(waypoint);
        }

        addMissionItems(missionItemsToAdd);
    }

    public double getLastAltitude() {
        if (!missionItemProxies.isEmpty()) {
            MissionItem lastItem = missionItemProxies.get(missionItemProxies.size() - 1).getMissionItem();
            if (lastItem instanceof MissionItem.SpatialItem
                    && !(lastItem instanceof RegionOfInterest)) {
                return ((MissionItem.SpatialItem) lastItem).getCoordinate().getAltitude();
            }
        }

        return DEFAULT_ALTITUDE;
    }

    /**
     * Add a set of spline waypoints generated around the passed 2D points.
     *
     * @param points list of points used as location for the spline waypoints
     */
    public void addSplineWaypoints(List<Coordinate> points) {
        final double alt = getLastAltitude();
        final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for (Coordinate point : points) {
            SplineWaypoint splineWaypoint = new SplineWaypoint();
            splineWaypoint.setCoordinate(new CoordinateExtend(point.getLatitude(), point.getLongitude(),
                    (float) alt));
            missionItemsToAdd.add(splineWaypoint);
        }

        addMissionItems(missionItemsToAdd);
    }

    private void addMissionItems(List<MissionItem> missionItems) {
        for (MissionItem missionItem : missionItems) {
            missionItemProxies.add(new MissionItemProxy(this, missionItem));
        }

        notifyMissionUpdate();
    }

    public void addSpatialWaypoint(BaseSpatialItem spatialItem, Coordinate point) {
        final double alt = getLastAltitude();
        spatialItem.setCoordinate(new CoordinateExtend(point.getLatitude(), point.getLongitude(), alt));
        addMissionItem(spatialItem);
    }

    /**
     * Add a waypoint generated around the passed 2D point.
     *
     * @param point point used to generate the mission waypoint
     */
    public void addWaypoint(Coordinate point) {
        final double alt = getLastAltitude();
        final Waypoint waypoint = new Waypoint();
        waypoint.setCoordinate(new CoordinateExtend(point.getLatitude(), point.getLongitude(), alt));
        addMissionItem(waypoint);
    }

    /**
     * Add a spline waypoint generated around the passed 2D point.
     *
     * @param point point used as location for the spline waypoint.
     */
    public void addSplineWaypoint(Coordinate point) {
        final double alt = getLastAltitude();
        final SplineWaypoint splineWaypoint = new SplineWaypoint();
        splineWaypoint.setCoordinate(new CoordinateExtend(point.getLatitude(), point.getLongitude(), alt));
        addMissionItem(splineWaypoint);
    }

    private void addMissionItem(MissionItem missionItem) {
        missionItemProxies.add(new MissionItemProxy(this, missionItem));
        notifyMissionUpdate();
    }

    private void addMissionItem(int index, MissionItem missionItem) {
        missionItemProxies.add(index, new MissionItemProxy(this, missionItem));
        notifyMissionUpdate();
    }

    public void addTakeoff() {
        Takeoff takeoff = new Takeoff();
        takeoff.setTakeoffAltitude(10);
        addMissionItem(takeoff);
    }

    public boolean hasTakeoffAndLandOrRTL() {
        if (missionItemProxies.size() >= 2) {
            if (isFirstItemTakeoff() && isLastItemLandOrRTL()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirstItemTakeoff() {
        return !missionItemProxies.isEmpty() && missionItemProxies.get(0).getMissionItem().getType() ==
                MissionItemType.TAKEOFF;
    }

    public boolean isLastItemLandOrRTL() {
        final int itemsCount = missionItemProxies.size();
        if (itemsCount == 0) return false;

        final MissionItemType itemType = missionItemProxies.get(itemsCount - 1).getMissionItem()
                .getType();
        return itemType == MissionItemType.RETURN_TO_LAUNCH || itemType == MissionItemType.LAND;
    }

    public void addTakeOffAndRTL() {
        if (!isFirstItemTakeoff()) {
            double defaultAlt = Takeoff.DEFAULT_TAKEOFF_ALTITUDE;
            if (!missionItemProxies.isEmpty()) {
                MissionItem firstItem = missionItemProxies.get(0).getMissionItem();
                if (firstItem instanceof MissionItem.SpatialItem)
                    defaultAlt = ((MissionItem.SpatialItem) firstItem).getCoordinate().getAltitude();
                else if (firstItem instanceof Survey) {
                    final SurveyDetail surveyDetail = ((Survey) firstItem).getSurveyDetail();
                    if (surveyDetail != null)
                        defaultAlt = surveyDetail.getAltitude();
                }
            }

            final Takeoff takeOff = new Takeoff();
            takeOff.setTakeoffAltitude(defaultAlt);
            addMissionItem(0, takeOff);
        }

        if (!isLastItemLandOrRTL()) {
            final ReturnToLaunch rtl = new ReturnToLaunch();
            addMissionItem(rtl);
        }
    }

    public int getOrder(MissionItemProxy item) {
        return missionItemProxies.indexOf(item) + 1;
    }

    /**
     * Updates a mission item render
     *
     * @param oldItem mission item render to update
     * @param newItem new mission item render
     */
    public void replace(MissionItemProxy oldItem, MissionItemProxy newItem) {
        final int index = missionItemProxies.indexOf(oldItem);
        if (index == -1)
            return;

        missionItemProxies.remove(index);
        missionItemProxies.add(index, newItem);

        if (selection.selectionContains(oldItem)) {
            selection.removeItemFromSelection(oldItem);
            selection.addToSelection(newItem);
        }

        notifyMissionUpdate();
    }

    public void replaceAll(List<Pair<MissionItemProxy, List<MissionItemProxy>>> oldNewList) {
        if (oldNewList == null) {
            return;
        }

        final int pairSize = oldNewList.size();
        if (pairSize == 0) {
            return;
        }

        final List<MissionItemProxy> selectionsToRemove = new ArrayList<MissionItemProxy>(pairSize);
        final List<MissionItemProxy> itemsToSelect = new ArrayList<MissionItemProxy>(pairSize);

        for (int i = 0; i < pairSize; i++) {
            final MissionItemProxy oldItem = oldNewList.get(i).first;
            final int index = missionItemProxies.indexOf(oldItem);
            if (index == -1) {
                continue;
            }

            missionItemProxies.remove(index);

            final List<MissionItemProxy> newItems = oldNewList.get(i).second;
            missionItemProxies.addAll(index, newItems);

            if (selection.selectionContains(oldItem)) {
                selectionsToRemove.add(oldItem);
                itemsToSelect.addAll(newItems);
            }
        }

        //Update the selection list.
        selection.removeItemsFromSelection(selectionsToRemove);
        selection.addToSelection(itemsToSelect);

        notifyMissionUpdate();
    }
    public void reverse() {
        Collections.reverse(missionItemProxies);
    }

    public void swap(int fromIndex, int toIndex) {
        MissionItemProxy from = missionItemProxies.get(fromIndex);
        MissionItemProxy to = missionItemProxies.get(toIndex);

        missionItemProxies.set(toIndex, from);
        missionItemProxies.set(fromIndex, to);
        notifyMissionUpdate();
    }

    public void clear() {
        selection.clearSelection();
        missionItemProxies.clear();
        notifyMissionUpdate();
    }

    public double getAltitudeDiffFromPreviousItem(MissionItemProxy waypointRender) {
        final int itemsCount = missionItemProxies.size();
        if (itemsCount < 2)
            return 0;

        MissionItem waypoint = waypointRender.getMissionItem();
        if (!(waypoint instanceof MissionItem.SpatialItem))
            return 0;

        final int index = missionItemProxies.indexOf(waypointRender);
        if (index == -1 || index == 0)
            return 0;

        MissionItem previous = missionItemProxies.get(index - 1).getMissionItem();
        if (previous instanceof MissionItem.SpatialItem) {
            return ((MissionItem.SpatialItem) waypoint).getCoordinate().getAltitude()
                    - ((MissionItem.SpatialItem) previous).getCoordinate().getAltitude();
        }

        return 0;
    }

    public double getDistanceFromLastWaypoint(MissionItemProxy waypointRender) {
        if (missionItemProxies.size() < 2)
            return 0;

        MissionItem waypoint = waypointRender.getMissionItem();
        if (!(waypoint instanceof MissionItem.SpatialItem))
            return 0;

        final int index = missionItemProxies.indexOf(waypointRender);
        if (index == -1 || index == 0)
            return 0;

        MissionItem previous = missionItemProxies.get(index - 1).getMissionItem();
        if (previous instanceof MissionItem.SpatialItem) {
            return MathUtils.getDistance(((MissionItem.SpatialItem) waypoint).getCoordinate(),
                    ((MissionItem.SpatialItem) previous).getCoordinate());
        }

        return 0;
    }

    @Override
    public List<Coordinate> getPathPoints() {
        if (missionItemProxies.isEmpty()) {
            return Collections.emptyList();
        }

        // Partition the mission items into spline/non-spline buckets.
        final List<Pair<Boolean, List<MissionItemProxy>>> bucketsList = new ArrayList<Pair<Boolean, List<MissionItemProxy>>>();

        boolean isSpline = false;
        List<MissionItemProxy> currentBucket = new ArrayList<MissionItemProxy>();
        for (MissionItemProxy missionItemProxy : missionItemProxies) {

            MissionItem missionItem = missionItemProxy.getMissionItem();
            if (missionItem instanceof MissionItem.Command) {
                //Skip commands
                continue;
            }

            if (missionItem instanceof SplineWaypoint) {
                if (!isSpline) {
                    if (!currentBucket.isEmpty()) {
                        // Get the last item from the current bucket. It will
                        // become the first
                        // anchor point for the spline path.
                        final MissionItemProxy lastItem = currentBucket
                                .get(currentBucket.size() - 1);

                        // Store the previous item bucket.
                        bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(Boolean.FALSE,
                                currentBucket));

                        // Create a new bucket for this category and update
                        // 'isSpline'
                        currentBucket = new ArrayList<MissionItemProxy>();
                        currentBucket.add(lastItem);
                    }

                    isSpline = true;
                }

                // Add the current element into the bucket
                currentBucket.add(missionItemProxy);
            } else {
                if (isSpline) {

                    // Add the current item to the spline bucket. It will act as
                    // the end anchor
                    // point for the spline path.
                    if (!currentBucket.isEmpty()) {
                        currentBucket.add(missionItemProxy);

                        // Store the previous item bucket.
                        bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(Boolean.TRUE,
                                currentBucket));

                        currentBucket = new ArrayList<MissionItemProxy>();
                    }

                    isSpline = false;
                }

                // Add the current element into the bucket
                currentBucket.add(missionItemProxy);
            }
        }

        bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(isSpline, currentBucket));

        final List<Coordinate> pathPoints = new ArrayList<Coordinate>();
        Coordinate lastPoint = null;

        for (Pair<Boolean, List<MissionItemProxy>> bucketEntry : bucketsList) {

            final List<MissionItemProxy> bucket = bucketEntry.second;
            if (bucketEntry.first) {
                final List<Coordinate> splinePoints = new ArrayList<Coordinate>();
                for (MissionItemProxy missionItemProxy : bucket) {
                    splinePoints.addAll(missionItemProxy.getPath(lastPoint));

                    if (!splinePoints.isEmpty()) {
                        lastPoint = splinePoints.get(splinePoints.size() - 1);
                    }
                }

                pathPoints.addAll(MathUtils.SplinePath.process(splinePoints));
            } else {
                for (MissionItemProxy missionItemProxy : bucket) {
                    pathPoints.addAll(missionItemProxy.getPath(lastPoint));

                    if (!pathPoints.isEmpty()) {
                        lastPoint = pathPoints.get(pathPoints.size() - 1);
                    }
                }
            }
        }

        return pathPoints;
    }

    public void removeSelection(MissionSelection missionSelection) {
        missionItemProxies.removeAll(missionSelection.mSelectedItems);
        missionSelection.clearSelection();
        notifyMissionUpdate();
    }

    public void move(MissionItemProxy item, Coordinate position) {
        MissionItem missionItem = item.getMissionItem();
        if (missionItem instanceof SpatialItem) {
            SpatialItem spatialItem = (SpatialItem) missionItem;
            spatialItem.setCoordinate(new CoordinateExtend(position.getLatitude(),
                    position.getLongitude(), spatialItem.getCoordinate().getAltitude()));

            if (spatialItem instanceof StructureScanner) {
                this.flight.buildMissionItemsAsync(missionItemsBuiltListener, (StructureScanner) spatialItem);
            }

            notifyMissionUpdate();
        }
    }

    public List<Coordinate> getVisibleCoords() {
        return getVisibleCoords(missionItemProxies);
    }

    public void movePolygonPoint(Survey survey, int index, Coordinate position) {
        survey.getPolygonPoints().get(index).set(position);
        this.flight.buildMissionItemsAsync(missionItemsBuiltListener, survey);
        notifyMissionUpdate();
    }

    public static List<Coordinate> getVisibleCoords(List<MissionItemProxy> mipList) {
        final List<Coordinate> coords = new ArrayList<Coordinate>();

        if (mipList == null || mipList.isEmpty()) {
            return coords;
        }

        for (MissionItemProxy itemProxy : mipList) {
            MissionItem item = itemProxy.getMissionItem();
            if (!(item instanceof SpatialItem))
                continue;

            final Coordinate coordinate = ((SpatialItem) item).getCoordinate();
            if (coordinate.getLatitude() == 0 || coordinate.getLongitude() == 0)
                continue;

            coords.add(coordinate);
        }

        return coords;
    }

    private Mission generateMission() {
        return generateMission(false);
    }

    private Mission generateMission(boolean isDeepCopy) {
        final Mission mission = new Mission();

        if (!missionItemProxies.isEmpty()) {
            for (MissionItemProxy itemProxy : missionItemProxies) {
                MissionItem sourceItem = itemProxy.getMissionItem();
                MissionItem destItem = isDeepCopy ? sourceItem.clone() : sourceItem;
                mission.addMissionItem(destItem);
            }
        }

        return mission;
    }

    public void sendMissionToAPM(Flight flight) {
        flight.setMission(generateMission(), true);

        final int missionItemsCount = missionItemProxies.size();

        String missionItemsList = "[";
        if (missionItemsCount > 0) {
            boolean isFirst = true;
            for (MissionItemProxy itemProxy : missionItemProxies) {
                if (isFirst)
                    isFirst = false;
                else
                    missionItemsList += ", ";

                missionItemsList += itemProxy.getMissionItem().getType().getLabel();
            }
        }

        missionItemsList += "]";

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MISSION_PLANNING)
                .setAction("Mission sent to flight")
                .setLabel("Mission items: " + missionItemsList);
        GAUtils.sendEvent(eventBuilder);

        //Send an event for the created mission
        eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MISSION_PLANNING)
                .setAction("Mission sent to flight")
                .setLabel("Mission items count")
                .setValue(missionItemsCount);
        GAUtils.sendEvent(eventBuilder);
    }

    public double getMissionLength() {
        List<Coordinate> points = getPathPoints();
        double length = 0;
        if (points.size() > 1) {
            for (int i = 1; i < points.size(); i++) {
                length += MathUtils.getDistance(points.get(i - 1), points.get(i));
            }
        }

        return length;
    }

    public void makeAndUploadFlight(Flight flight) {
        flight.generateFlight();
    }

    public List<List<Coordinate>> getPolygonsPath() {
        ArrayList<List<Coordinate>> polygonPaths = new ArrayList<List<Coordinate>>();
        for (MissionItemProxy itemProxy : missionItemProxies) {
            MissionItem item = itemProxy.getMissionItem();
            if (item instanceof Survey) {
                polygonPaths.add(((Survey) item).getPolygonPoints());
            }
        }
        return polygonPaths;
    }

    public boolean writeMissionToFile(String filename) {
        return MissionWriter.write(generateMission(), filename);
    }

    public boolean readMissionFromFile(huins.ex.util.File.IO.MissionReader reader) {
        if (reader == null)
            return false;

        Mission mission = reader.getMission();
        flight.setMission(mission, false);

        load(mission);
        return true;
    }
}

