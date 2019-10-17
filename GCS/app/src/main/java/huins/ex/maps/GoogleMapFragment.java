package huins.ex.maps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.location.LocationListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import huins.ex.R;
import huins.ex.helper.LocalMapTileProvider;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.AutoPanMode;
import huins.ex.proto.Coordinate;
import huins.ex.proto.Flight;
import huins.ex.proto.MapInterface;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Gps;
import huins.ex.util.FlightLocationHelper;
import huins.ex.util.GoogleApiClientManager;
import huins.ex.util.GoogleApiClientManager.GoogleApiClientTask;
import huins.ex.util.HashBiMap;
import huins.ex.util.constants.GCSPreferences;
import huins.ex.view.activities.GCSBaseApp;

/**
 * Created by suhak on 15. 6. 23.
 */
public class GoogleMapFragment extends SupportMapFragment implements MapInterface, LocationListener, GoogleApiClientManager.ManagerListener {

    private static final String TAG = GoogleMapFragment.class.getSimpleName();

    public static final String PREF_MAP_TYPE = "pref_map_type";

    public static final String MAP_TYPE_SATELLITE = "Satellite";
    public static final String MAP_TYPE_HYBRID = "Hybrid";
    public static final String MAP_TYPE_NORMAL = "Normal";
    public static final String MAP_TYPE_TERRAIN = "Terrain";

    // TODO: update the interval based on the user's current activity.
    private static final long USER_LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final long USER_LOCATION_UPDATE_FASTEST_INTERVAL = 1000; // ms
    private static final float USER_LOCATION_UPDATE_MIN_DISPLACEMENT = 0; // m

    public static final int REQUEST_CHECK_SETTINGS = 147;

    private static final float GO_TO_MY_LOCATION_ZOOM = 17f;

    private GoogleApiClientManager mGApiClientMgr;

    protected boolean useMarkerClickAsMapClick = false;

    private android.location.LocationListener mLocationListener;

    private Marker userMarker;
    private final HashBiMap<MarkerInfo, Marker> mBiMarkersMap = new HashBiMap<MarkerInfo, Marker>();

    private GCSPreferences gcsPrefs;

    protected GCSBaseApp gcsBaseApp;

    //event listener
    private MapInterface.OnMapClickListener mMapClickListener;
    private MapInterface.OnMapLongClickListener mMapLongClickListener;
    private MapInterface.OnMarkerClickListener mMarkerClickListener;
    private MapInterface.OnMarkerDragListener mMarkerDragListener;

    private Polygon footprintPoly;
    private List<Polygon> polygonsPaths = new ArrayList<Polygon>();
    //path
    private Polyline flightPath;
    private Polyline missionPath;
    private Polyline mDroneLeashPath;
    private int maxFlightPathSize;

    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(
            AutoPanMode.DISABLED);

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        //eventFilter.addAction(SettingsFragment.ACTION_MAP_ROTATION_PREFERENCE_UPDATED);
        //eventFilter.addAction(SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED);
    }

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{LocationServices.API};

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.GPS_POSITION:
                    if (mPanMode.get() == AutoPanMode.DRONE) {
                        final Flight flight = getFlightInterface();
                        if (!flight.IsConnected())
                            return;

                        final Gps droneGps = flight.getAttribute(AttributeType.GPS);
                        if (droneGps != null && droneGps.isValid()) {
                            final Coordinate droneLocation = droneGps.getPosition();
                            updateCamera(droneLocation);
                        }
                    }
                    break;
/*
                case SettingsFragment.ACTION_MAP_ROTATION_PREFERENCE_UPDATED:
                    getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            setupMapUI(googleMap);
                        }
                    });
                    break;

                case SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED:
                    final int resultCode = intent.getIntExtra(SettingsFragment.EXTRA_RESULT_CODE, Activity.RESULT_OK);
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            // All required changes were successfully made. Try to acquire user location again
                            mGApiClientMgr.addTask(mRequestLocationUpdateTask);
                            break;

                        case Activity.RESULT_CANCELED:
                            // The user was asked to change settings, but chose not to
                            Toast.makeText(getActivity(), "Please update your location settings!", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    break;
            */
            }
        }
    };

    private Flight getFlightInterface() {
        return gcsBaseApp.getFlight();
    }


    private void setupMap() {
        // Make sure the map is initialized
        MapsInitializer.initialize(getActivity().getApplicationContext());

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMapUI(googleMap);
                setupMapOverlay(googleMap);
                setupMapListeners(googleMap);
            }
        });
    }

    private void setupMapUI(GoogleMap map) {
        map.setMyLocationEnabled(false);
        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setRotateGesturesEnabled(gcsPrefs.isMapRotationEnabled());
    }
    private void setupMapOverlay(GoogleMap map) {

        if (gcsPrefs.isOfflineMapEnabled()) {
            setupOfflineMapOverlay(map);
        } else {
            setupOnlineMapOverlay(map);
        }
    }
    private void setupOnlineMapOverlay(GoogleMap map) {
        map.setMapType(getMapType());
    }
    private int getMapType() {
        String mapType = gcsPrefs.getMapType();

        if (mapType.equalsIgnoreCase(MAP_TYPE_SATELLITE)) {
            return GoogleMap.MAP_TYPE_SATELLITE;
        }
        if (mapType.equalsIgnoreCase(MAP_TYPE_HYBRID)) {
            return GoogleMap.MAP_TYPE_HYBRID;
        }
        if (mapType.equalsIgnoreCase(MAP_TYPE_NORMAL)) {
            return GoogleMap.MAP_TYPE_NORMAL;
        }
        if (mapType.equalsIgnoreCase(MAP_TYPE_TERRAIN)) {
            return GoogleMap.MAP_TYPE_TERRAIN;
        } else {
            return GoogleMap.MAP_TYPE_NORMAL;
        }
    }

    private void setupMapListeners(GoogleMap googleMap) {

        final GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMapClickListener != null) {
                    mMapClickListener.onMapClick(FlightLocationHelper.LatLngToCoord(latLng));
                }
            }
        };
        googleMap.setOnMapClickListener(onMapClickListener);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick(FlightLocationHelper.LatLngToCoord(latLng));
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(FlightLocationHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragStart(markerInfo);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(FlightLocationHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDrag(markerInfo);
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(FlightLocationHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (useMarkerClickAsMapClick) {
                    onMapClickListener.onMapClick(marker.getPosition());
                    return true;
                }

                if (mMarkerClickListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    if(markerInfo != null)
                        return mMarkerClickListener.onMarkerClick(markerInfo);
                }
                return false;
            }
        });
    }

    //API task
    private final GoogleApiClientTask requestLastLocationTask = new GoogleApiClientTask() {
        @Override
        protected void doRun() {
            final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
            if (lastLocation != null && mLocationListener != null) {
                mLocationListener.onLocationChanged(lastLocation);
            }
        }
    };

    private final GoogleApiClientTask mGoToMyLocationTask = new GoogleApiClientTask() {
        @Override
        public void doRun() {
            final Location myLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
            if (myLocation != null) {
                updateCamera(FlightLocationHelper.LocationToCoord(myLocation), GO_TO_MY_LOCATION_ZOOM);

                if (mLocationListener != null)
                    mLocationListener.onLocationChanged(myLocation);
            }
        }
    };
    private final GoogleApiClientTask mRemoveLocationUpdateTask = new GoogleApiClientTask() {
        @Override
        public void doRun() {
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), GoogleMapFragment.this);
        }
    };

    private final GoogleApiClientTask mRequestLocationUpdateTask = new GoogleApiClientTask() {
        @Override
        public void doRun() {
            final LocationRequest locationReq = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(USER_LOCATION_UPDATE_FASTEST_INTERVAL)
                    .setInterval(USER_LOCATION_UPDATE_INTERVAL)
                    .setSmallestDisplacement(USER_LOCATION_UPDATE_MIN_DISPLACEMENT);

            final GoogleApiClient googleApiClient = getGoogleApiClient();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationReq);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult callback) {
                    final Status status = callback.getStatus();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationReq,
                                    GoogleMapFragment.this);
                            break;

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                                Log.e(TAG, e.getMessage(), e);
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Log.w(TAG, "Unable to get accurate user location.");
                            Toast.makeText(getActivity(), "Unable to get accurate location. Please update your " +
                                    "location settings!", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });
        }
    };

    private void setupOfflineMapOverlay(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        TileOverlay tileOverlay = map.addTileOverlay(new TileOverlayOptions()
                .tileProvider(new LocalMapTileProvider()));
        tileOverlay.setZIndex(-1);
        tileOverlay.clearTileCache();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        gcsBaseApp = (GCSBaseApp) activity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        final FragmentActivity activity = getActivity();
        final Context context = activity.getApplicationContext();

        final View view = super.onCreateView(inflater, viewGroup, bundle);

        mGApiClientMgr = new GoogleApiClientManager(context, new Handler(), apisList);
        mGApiClientMgr.setManagerListener(this);

        gcsPrefs = new GCSPreferences(context);

        final Bundle args = getArguments();
        if (args != null) {
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        mGApiClientMgr.start();

        mGApiClientMgr.addTask(mRequestLocationUpdateTask);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .registerReceiver(eventReceiver, eventFilter);

        setupMap();
    }

    @Override
    public void onStop() {
        super.onStop();

        mGApiClientMgr.addTask(mRemoveLocationUpdateTask);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .unregisterReceiver(eventReceiver);

        mGApiClientMgr.stopSafely();
    }

    @Override
    public void addFlightPathPoint(Coordinate coord) {

    }

    @Override
    public void clearMarkers() {

    }

    @Override
    public void clearFlightPath() {

    }

    @Override
    public Coordinate getMapCenter() {
        return null;
    }

    @Override
    public float getMapZoomLevel() {
        return getMap().getCameraPosition().zoom;
    }

    @Override
    public Set<MarkerInfo> getMarkerInfoList() {
        return new HashSet<MarkerInfo>(mBiMarkersMap.keySet());
    }

    @Override
    public float getMaxZoomLevel() {
        return getMap().getMaxZoomLevel();
    }

    @Override
    public float getMinZoomLevel() {
        return getMap().getMinZoomLevel();
    }
    @Override
    public MapProvider getProvider() {
        return MapProvider.GOOGLE_MAP;
    }

    @Override
    public void goToDroneLocation() {
        final Flight flight = getFlightInterface();
        if (!flight.IsConnected()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.flight_disconnected, Toast.LENGTH_SHORT).show();
            return;
        }

        final Gps droneGps = flight.getAttribute(AttributeType.GPS);

        if (!droneGps.isValid()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.drone_no_location, Toast.LENGTH_SHORT).show();
            return;
        }
        /* temp code in case there is no gps value from drone (0, 0)*/
        if (droneGps.getPosition().getLongitude() == 0 && droneGps.getPosition().getLongitude() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.drone_no_location, Toast.LENGTH_SHORT).show();
            final Coordinate tempLocation = droneGps.getPosition();
            tempLocation.setLatitude(userMarker.getPosition().latitude);
            tempLocation.setLongitude(userMarker.getPosition().longitude);
            updateCamera(tempLocation, (int) getMap().getCameraPosition().zoom);
            return;
        }
        final float currentZoomLevel = getMap().getCameraPosition().zoom;
        final Coordinate droneLocation = droneGps.getPosition();
        updateCamera(droneLocation, (int) currentZoomLevel);
    }

    @Override
    public void goToMyLocation() {
       if (!mGApiClientMgr.addTask(mGoToMyLocationTask)) {
            Log.e(TAG, "Unable to add google api client task.");
        }
    }

    @Override
    public void loadCameraPosition() {

    }

    @Override
    public List<Coordinate> projectPathIntoMap(List<Coordinate> pathPoints) {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        Projection projection = getMap().getProjection();

        for (Coordinate point : pathPoints) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getLatitude(), (int) point.getLongitude()));
            coords.add(FlightLocationHelper.LatLngToCoord(coord));
        }

        return coords;
    }

    @Override
    public void removeMarkers(Collection<MarkerInfo> markerInfoList) {
        if (markerInfoList == null || markerInfoList.isEmpty()) {
            return;
        }

        for (MarkerInfo markerInfo : markerInfoList) {
            Marker marker = mBiMarkersMap.getValue(markerInfo);
            if (marker != null) {
                marker.remove();
                mBiMarkersMap.removeKey(markerInfo);
            }
        }
    }

    @Override
    public void saveCameraPosition() {
        CameraPosition camera = getMap().getCameraPosition();
        gcsPrefs.prefs.edit()
                .putFloat(PREF_LAT, (float) camera.target.latitude)
                .putFloat(PREF_LNG, (float) camera.target.longitude)
                .putFloat(PREF_BEA, camera.bearing)
                .putFloat(PREF_TILT, camera.tilt)
                .putFloat(PREF_ZOOM, camera.zoom).apply();
    }

    @Override
    public void selectAutoPanMode(AutoPanMode mode) {
        final AutoPanMode currentMode = mPanMode.get();
        if (currentMode == mode)
            return;

        setAutoPanMode(currentMode, mode);
    }

    @Override
    public void setMapPadding(int left, int top, int right, int bottom) {
        getMap().setPadding(left, top, right, bottom);
    }

    @Override
    public void setOnMapClickListener(OnMapClickListener listener) {
        mMapClickListener = listener;
    }

    @Override
    public void setOnMapLongClickListener(OnMapLongClickListener listener) {
        mMapLongClickListener = listener;
    }

    @Override
    public void setOnMarkerDragListener(OnMarkerDragListener listener) {
        mMarkerDragListener = listener;
    }

    @Override
    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        mMarkerClickListener = listener;
    }

    @Override
    public void setLocationListener(android.location.LocationListener listener) {
        mLocationListener = listener;
        if (mLocationListener != null) {
            mGApiClientMgr.addTask(requestLastLocationTask);
        }
    }

    private void updateCamera(final Coordinate coord) {
        if (coord != null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    final float zoomLevel = googleMap.getCameraPosition().zoom;
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(FlightLocationHelper.CoordToLatLang(coord),
                            zoomLevel));
                }
            });
        }
    }


    @Override
    public void updateCamera(final Coordinate coord,final float zoomLevel) {
        if (coord != null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            FlightLocationHelper.CoordToLatLang(coord), zoomLevel));
                }
            });
        }
    }

    @Override
    public void updateCameraBearing(float bearing) {
        final CameraPosition cameraPosition = new CameraPosition(FlightLocationHelper.CoordToLatLang
                (getMapCenter()), getMapZoomLevel(), 0, bearing);
        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource) {
        List<Coordinate> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (Coordinate coord : pathCoords) {
            pathPoints.add(FlightLocationHelper.CoordToLatLang(coord));
        }

        if (mDroneLeashPath == null) {
            PolylineOptions flightPath = new PolylineOptions();
            flightPath.color(DRONE_LEASH_DEFAULT_COLOR).width(
                    FlightLocationHelper.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH,
                            getResources()));
            mDroneLeashPath = getMap().addPolyline(flightPath);
        }

        mDroneLeashPath.setPoints(pathPoints);
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo) {
        updateMarker(markerInfo, markerInfo.isDraggable());
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable) {
        // if the drone hasn't received a gps signal yet
        final Coordinate coord = markerInfo.getPosition();
        if (coord == null) {
            return;
        }

        final LatLng position = FlightLocationHelper.CoordToLatLang(coord);
        Marker marker = mBiMarkersMap.getValue(markerInfo);
        if (marker == null) {
            // Generate the marker
            generateMarker(markerInfo, position, isDraggable);
        } else {
            // Update the marker
            updateMarker(marker, markerInfo, position, isDraggable);
        }
    }


    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos) {
        for (MarkerInfo info : markersInfos) {
            updateMarker(info);
        }
    }

    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos, boolean isDraggable) {
        for (MarkerInfo info : markersInfos) {
            updateMarker(info, isDraggable);
        }
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        List<Coordinate> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (Coordinate coord : pathCoords) {
            pathPoints.add(FlightLocationHelper.CoordToLatLang(coord));
        }

        if (missionPath == null) {
            PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(
                    MISSION_PATH_DEFAULT_WIDTH);
            missionPath = getMap().addPolyline(pathOptions);
        }

        missionPath.setPoints(pathPoints);
    }


    @Override
    public void updatePolygonsPaths(List<List<Coordinate>> paths) {
        for (Polygon poly : polygonsPaths) {
            poly.remove();
        }

        for (List<Coordinate> contour : paths) {
            PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.strokeColor(POLYGONS_PATH_DEFAULT_COLOR).strokeWidth(
                    POLYGONS_PATH_DEFAULT_WIDTH);
            final List<LatLng> pathPoints = new ArrayList<LatLng>(contour.size());
            for (Coordinate coord : contour) {
                pathPoints.add(FlightLocationHelper.CoordToLatLang(coord));
            }
            pathOptions.addAll(pathPoints);
            polygonsPaths.add(getMap().addPolygon(pathOptions));
        }

    }


    @Override
    public void zoomToFit(List<Coordinate> coords) {
        if (!coords.isEmpty()) {
            final List<LatLng> points = new ArrayList<LatLng>();
            for (Coordinate coord : coords)
                points.add(FlightLocationHelper.CoordToLatLang(coord));

            final LatLngBounds bounds = getBounds(points);
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    final Activity activity = getActivity();
                    if(activity == null)
                        return;

                    final View rootView = ((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);
                    if(rootView == null)
                        return;

                    final int height = rootView.getHeight();
                    final int width = rootView.getWidth();
                    Log.d(TAG, String.format(Locale.US, "Screen W %d, H %d", width, height));
                    if(height > 0 && width > 0) {
                        CameraUpdate animation = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100);
                        googleMap.animateCamera(animation);
                    }
                }
            });
        }
    }

    @Override
    public void zoomToFitMyLocation(final List<Coordinate> coords) {
        mGApiClientMgr.addTask(new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final Location myLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
                if (myLocation != null) {
                    final List<Coordinate> updatedCoords = new ArrayList<Coordinate>(coords);
                    updatedCoords.add(FlightLocationHelper.LocationToCoord(myLocation));
                    zoomToFit(updatedCoords);
                } else {
                    zoomToFit(coords);
                }
            }
        });
    }

    @Override
    public void skipMarkerClickEvents(boolean skip) {
        useMarkerClickAsMapClick = skip;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Update the user location icon.
        if (userMarker == null) {
            final MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .draggable(false)
                    .flat(true)
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue));

            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    userMarker = googleMap.addMarker(options);
                }
            });
        } else {
            userMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        if (mPanMode.get() == AutoPanMode.USER) {
            Log.d(TAG, "User location changed.");
            //    updateCamera(DroneHelper.LocationToCoord(location), (int) getMap().getCameraPosition().zoom);
        }

        if (mLocationListener != null) {
            mLocationListener.onLocationChanged(location);
        }
    }

    @Override
    public void onGoogleApiConnectionError(ConnectionResult result) {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, 0);
            } catch (IntentSender.SendIntentException e) {
                //There was an error with the resolution intent. Try again.
                if (mGApiClientMgr != null)
                    mGApiClientMgr.start();
            }
        } else {
            onUnavailableGooglePlayServices(result.getErrorCode());
        }
    }

    @Override
    public void onUnavailableGooglePlayServices(int status) {
        final Activity activity = getActivity();
        if (activity != null) {
            GooglePlayServicesUtil.showErrorDialogFragment(status, getActivity(), 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    activity.finish();
                }
            });
        }
    }

    @Override
    public void onManagerStarted() {

    }

    @Override
    public void onManagerStopped() {

    }

    private void setAutoPanMode(AutoPanMode current, AutoPanMode update) {
        if (mPanMode.compareAndSet(current, update)) {
            switch (current) {
                case DRONE:
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                            .unregisterReceiver(eventReceiver);
                    break;

                case DISABLED:
                default:
                    break;
            }

            switch (update) {
                case DRONE:
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver
                            (eventReceiver, eventFilter);
                    break;

                case DISABLED:
                default:
                    break;
            }
        }
    }
    private void generateMarker(MarkerInfo markerInfo, LatLng position, boolean isDraggable) {
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .draggable(isDraggable)
                .alpha(markerInfo.getAlpha())
                .anchor(markerInfo.getAnchorU(), markerInfo.getAnchorV())
                .infoWindowAnchor(markerInfo.getInfoWindowAnchorU(),
                        markerInfo.getInfoWindowAnchorV()).rotation(markerInfo.getRotation())
                .snippet(markerInfo.getSnippet()).title(markerInfo.getTitle())
                .flat(markerInfo.isFlat()).visible(markerInfo.isVisible());

        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        Marker marker = getMap().addMarker(markerOptions);
        mBiMarkersMap.put(markerInfo, marker);
    }

    private void updateMarker(Marker marker, MarkerInfo markerInfo, LatLng position,
                              boolean isDraggable) {
        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        marker.setAlpha(markerInfo.getAlpha());
        marker.setAnchor(markerInfo.getAnchorU(), markerInfo.getAnchorV());
        marker.setInfoWindowAnchor(markerInfo.getInfoWindowAnchorU(),
                markerInfo.getInfoWindowAnchorV());
        marker.setPosition(position);
        marker.setRotation(markerInfo.getRotation());
        marker.setSnippet(markerInfo.getSnippet());
        marker.setTitle(markerInfo.getTitle());
        marker.setDraggable(isDraggable);
        marker.setFlat(markerInfo.isFlat());
        marker.setVisible(markerInfo.isVisible());
    }

    private LatLngBounds getBounds(List<LatLng> pointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : pointsList) {
            builder.include(point);
        }
        return builder.build();
    }

    public double getMapRotation() {
        GoogleMap map = getMap();
        if (map != null) {
            return map.getCameraPosition().bearing;
        } else {
            return 0;
        }
    }
}
