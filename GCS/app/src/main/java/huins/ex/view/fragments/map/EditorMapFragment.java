package huins.ex.view.fragments.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import huins.ex.interfaces.MarkerInfo;
import huins.ex.model.markers.MissionItemMarkerInfo;
import huins.ex.model.markers.PolygonMarkerInfo;
import huins.ex.proto.AutoPanMode;
import huins.ex.proto.Coordinate;
import huins.ex.proto.MapInterface;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.property.Home;

/**
 * Created by suhak on 15. 6. 23.
 */
public class EditorMapFragment extends FlightMap implements MapInterface.OnMapClickListener, MapInterface.OnMapLongClickListener, MapInterface.OnMarkerClickListener, MapInterface.OnMarkerDragListener{

    private OnEditorInteraction editorListener;
   // protected MapInterface mMapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(inflater, viewGroup, bundle);

        mapInterface.setOnMarkerDragListener(this);
        mapInterface.setOnMarkerClickListener(this);
        mapInterface.setOnMapClickListener(this);
        mapInterface.setOnMapLongClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(!(activity instanceof OnEditorInteraction)){
            throw new IllegalStateException("Parent activity must implement " +
                    OnEditorInteraction.class.getName());
        }

        editorListener = (OnEditorInteraction) activity;
    }


    @Override
    public boolean onMarkerClick(MarkerInfo markerInfo) {
        if (markerInfo instanceof MissionItemMarkerInfo) {
            editorListener.onItemClick(((MissionItemMarkerInfo) markerInfo).getMarkerOrigin(), false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onMapLongClick(Coordinate point) {
    }

    @Override
    public void onMarkerDrag(MarkerInfo markerInfo) {
        checkForWaypointMarkerMoving(markerInfo);
    }

    @Override
    public void onMarkerDragStart(MarkerInfo markerInfo) {
        checkForWaypointMarkerMoving(markerInfo);
    }

    private void checkForWaypointMarkerMoving(MarkerInfo markerInfo) {
        if (markerInfo instanceof MissionItem.SpatialItem) {
            Coordinate position = markerInfo.getPosition();

            // update marker source
            MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) markerInfo;
            Coordinate waypointPosition = waypoint.getCoordinate();
            waypointPosition.setLatitude(position.getLatitude());
            waypointPosition.setLongitude(position.getLongitude());

            // update flight path
            mapInterface.updateMissionPath(missionProxy);
        }
    }


    @Override
    public boolean setAutoPanMode(AutoPanMode target) {
        if (target == AutoPanMode.DISABLED)
            return true;

        Toast.makeText(getActivity(), "Auto pan is not supported on this map.", Toast.LENGTH_LONG)
                .show();
        return false;
    }

    @Override
    public void onMarkerDragEnd(MarkerInfo markerInfo) {
        checkForWaypointMarker(markerInfo);
    }


    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

    @Override
    public void onMapClick(Coordinate coord) {
        editorListener.onMapClick(coord);
    }

    public void zoomToFit() {
        // get visible mission coords
        final List<Coordinate> visibleCoords = missionProxy.getVisibleCoords();

        // add home coord if visible

        Home home = flight.getAttribute(AttributeType.HOME);
        if(home != null ) {
            final Coordinate homeCoord = home.getCoordinate();
            if (homeCoord != null && homeCoord.getLongitude() != 0 && homeCoord.getLatitude() != 0)
                visibleCoords.add(homeCoord);
        }

        zoomToFit(visibleCoords);
    }

    public void zoomToFit(List<Coordinate> itemsToFit){
        if(!itemsToFit.isEmpty()){
            mapInterface.zoomToFit(itemsToFit);
        }
    }

    public List<Coordinate> projectPathIntoMap(List<Coordinate> path) {
        return mapInterface.projectPathIntoMap(path);
    }


    public void skipMarkerClickEvents(boolean skip) {
        mapInterface.skipMarkerClickEvents(skip);
    }

    private void checkForWaypointMarker(MarkerInfo markerInfo) {
        if ((markerInfo instanceof MissionItemMarkerInfo)) {
            missionProxy.move(((MissionItemMarkerInfo) markerInfo).getMarkerOrigin(),
                    markerInfo.getPosition());
        }else if ((markerInfo instanceof PolygonMarkerInfo)) {
            PolygonMarkerInfo marker = (PolygonMarkerInfo) markerInfo;
            missionProxy.movePolygonPoint(marker.getSurvey(), marker.getIndex(), markerInfo.getPosition());
        }
    }

}
