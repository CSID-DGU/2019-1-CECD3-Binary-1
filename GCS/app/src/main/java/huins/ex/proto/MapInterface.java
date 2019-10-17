package huins.ex.proto;

import android.graphics.Color;
import android.location.LocationListener;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import huins.ex.interfaces.MarkerInfo;
import huins.ex.maps.MapProvider;

/**
 * Created by suhak on 15. 6. 23.
 */
public interface MapInterface {
    public static final String PACKAGE_NAME = MapInterface.class.getPackage().getName();

    public static final String EXTRA_MAX_FLIGHT_PATH_SIZE = PACKAGE_NAME + ""
            + ".EXTRA_MAX_FLIGHT_PATH_SIZE";

    public static final int FLIGHT_PATH_DEFAULT_COLOR = 0xfffd693f;
    public static final int FLIGHT_PATH_DEFAULT_WIDTH = 6;

    public static final int MISSION_PATH_DEFAULT_COLOR = Color.WHITE;
    public static final int MISSION_PATH_DEFAULT_WIDTH = 4;

    public static final int DRONE_LEASH_DEFAULT_COLOR = Color.WHITE;
    public static final int DRONE_LEASH_DEFAULT_WIDTH = 2;

    public static final int POLYGONS_PATH_DEFAULT_COLOR = Color.RED;
    public static final int POLYGONS_PATH_DEFAULT_WIDTH = 4;

    public static final int FOOTPRINT_DEFAULT_COLOR = 0;
    public static final int FOOTPRINT_DEFAULT_WIDTH = 2;
    public static final int FOOTPRINT_FILL_COLOR = Color.argb(80, 0, 0, 200);

    public static final String PREF_LAT = "pref_map_lat";
    public static final float DEFAULT_LATITUDE = 37.8575523f;

    public static final String PREF_LNG = "pref_map_lng";
    public static final float DEFAULT_LONGITUDE = -122.292767f;

    public static final String PREF_BEA = "pref_map_bea";
    public static final int DEFAULT_BEARING = 0;

    public static final String PREF_TILT = "pref_map_tilt";
    public static final int DEFAULT_TILT = 0;

    public static final String PREF_ZOOM = "pref_map_zoom";
    public static final int DEFAULT_ZOOM_LEVEL = 17;

    interface PathSource {
        public List<Coordinate> getPathPoints();
    }

    interface OnMapClickListener {
        void onMapClick(Coordinate coord);
    }

    interface OnMapLongClickListener {
        void onMapLongClick(Coordinate coord);
    }

    interface OnMarkerClickListener {
        boolean onMarkerClick(MarkerInfo markerInfo);
    }

    interface OnMarkerDragListener {
        void onMarkerDrag(MarkerInfo markerInfo);
        void onMarkerDragEnd(MarkerInfo markerInfo);
        void onMarkerDragStart(MarkerInfo markerInfo);

    }

    public void addFlightPathPoint(Coordinate coord);

    //public void addCameraFootprint(FootPrint footprintToBeDraw);

    public void clearMarkers();

    public void clearFlightPath();

    public Coordinate getMapCenter();

    public float getMapZoomLevel();

    public Set<MarkerInfo> getMarkerInfoList();

    public float getMaxZoomLevel();

    public float getMinZoomLevel();

    public MapProvider getProvider();

    public void goToDroneLocation();

    public void goToMyLocation();

    public void loadCameraPosition();

    public List<Coordinate> projectPathIntoMap(List<Coordinate> pathPoints);

    public void removeMarkers(Collection<MarkerInfo> markerInfoList);

    public void saveCameraPosition();

    public void selectAutoPanMode(AutoPanMode mode);

    public void setMapPadding(int left, int top, int right, int bottom);

    public void setOnMapClickListener(OnMapClickListener listener); 

    public void setOnMapLongClickListener(OnMapLongClickListener listener);

    public void setOnMarkerClickListener(OnMarkerClickListener listener);

    public void setOnMarkerDragListener(OnMarkerDragListener listener);

    public void setLocationListener(LocationListener listener);

    public void updateCamera(Coordinate coord, float zoomLevel);

    public void updateCameraBearing(float bearing);

    public void updateDroneLeashPath(PathSource pathSource);

    public void updateMarker(MarkerInfo markerInfo);

    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable);

    public void updateMarkers(List<MarkerInfo> markersInfos);

    public void updateMarkers(List<MarkerInfo> markersInfos, boolean isDraggable);

    public void updateMissionPath(PathSource pathSource);

    public void updatePolygonsPaths(List<List<Coordinate>> paths);

    public void zoomToFit(List<Coordinate> coords);

    public void zoomToFitMyLocation(List<Coordinate> coords);

    public void skipMarkerClickEvents(boolean skip);

    //public void updateRealTimeFootprint(FootPrint footprint);
}
