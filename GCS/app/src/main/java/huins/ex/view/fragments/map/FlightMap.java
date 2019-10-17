package huins.ex.view.fragments.map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.maps.MapProvider;
import huins.ex.model.MissionProxy;
import huins.ex.proto.AutoPanMode;
import huins.ex.proto.Flight;
import huins.ex.proto.MapInterface;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.CameraProxy;
import huins.ex.proto.property.Gps;
import huins.ex.util.UIUtils;
import huins.ex.util.constants.GCSPreferences;
import huins.ex.view.fragments.helper.ApiListenerFragment;
import huins.ex.view.icons.GraphicFlight;
import huins.ex.view.icons.GraphicGuided;
import huins.ex.view.icons.GraphicHome;

/**
 * Created by suhak on 15. 6. 22.
 */
public abstract class FlightMap extends ApiListenerFragment {

    private final static String TAG = FlightMap.class.getSimpleName();

    private static final List<MarkerInfo> NO_EXTERNAL_MARKERS = Collections.emptyList();

    private final ConcurrentLinkedQueue<MapMarkerProvider> markerProviders = new ConcurrentLinkedQueue<>();

    public static final String ACTION_UPDATE_MAP = "action.UPDATE_MAP";

    private GraphicHome home;
    public GraphicFlight graphicFlight;
    public GraphicGuided guided;

    protected MapInterface mapInterface;

    protected GCSPreferences GCSPrefs;

    protected MissionProxy missionProxy;
    public Flight flight;

    protected Context context;

    private final Handler mHandler = new Handler();

    protected abstract boolean isMissionDraggable();

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.GUIDED_POINT_UPDATED);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_FIRST);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_RESTORED);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_TIMEOUT);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.CAMERA_FOOTPRINTS_UPDATED);
        eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
        eventFilter.addAction(ACTION_UPDATE_MAP);
    }


    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isResumed())
                return;
            final String action = intent.getAction();
            switch (action) {
                case ACTION_UPDATE_MAP:
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    postUpdate();
                    break;

                case AttributeEvent.GPS_POSITION: {
                    mapInterface.updateMarker(graphicFlight);
                    mapInterface.updateDroneLeashPath(guided);
                    final Gps droneGps = flight.getAttribute(AttributeType.GPS);
                    if (droneGps != null && droneGps.isValid()) {
                        mapInterface.addFlightPathPoint(droneGps.getPosition());
                    }
                    break;
                }

                case AttributeEvent.GUIDED_POINT_UPDATED:
                    mapInterface.updateMarker(guided);
                    mapInterface.updateDroneLeashPath(guided);
                    break;

                case AttributeEvent.HEARTBEAT_FIRST:
                case AttributeEvent.HEARTBEAT_RESTORED:
                    mapInterface.updateMarker(graphicFlight);
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.HEARTBEAT_TIMEOUT:
                    mapInterface.updateMarker(graphicFlight);
                    break;

                case AttributeEvent.CAMERA_FOOTPRINTS_UPDATED: {
                    CameraProxy camera = flight.getAttribute(AttributeType.CAMERA);
                    //if (camera != null && camera.getLastFootPrint() != null)
                        //mapInterface.addCameraFootprint(camera.getLastFootPrint());
                    break;
                }

                case AttributeEvent.ATTITUDE_UPDATED: {
                    if (GCSPrefs.isRealtimeFootprintsEnabled()) {
                        final Gps droneGps = flight.getAttribute(AttributeType.GPS);
                        if (droneGps.isValid()) {
                            CameraProxy camera = flight.getAttribute(AttributeType.CAMERA);
                          //  if (camera != null && camera.getCurrentFieldOfView() != null)
                       //         mapInterface.updateRealTimeFootprint(camera.getCurrentFieldOfView());
                        }

                    } else {
                    //    mapInterface.updateRealTimeFootprint(null);
                    }
                    break;
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        final View view = inflater.inflate(R.layout.fragment_flight_map, viewGroup, false);
        GCSPrefs = new GCSPreferences(context);
        updateMapFragment();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeCallbacksAndMessages(null);
    }
    @Override
    public void onPause() {
        super.onPause();
        mapInterface.saveCameraPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapInterface.loadCameraPosition();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMapFragment();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onApiConnected() {
        if (mapInterface != null)
            mapInterface.clearMarkers();

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);

        flight = getFlight();
        missionProxy = getMissionProxy();

        home = new GraphicHome(flight);
        graphicFlight = new GraphicFlight(flight);
        guided = new GraphicGuided(flight);

        postUpdate();
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity.getApplicationContext();
    }

    public final void postUpdate() {
        mHandler.post(mUpdateMap);
    }

    public void goToMyLocation() {
        mapInterface.goToMyLocation();
    }

    public void goToFlightLocation() {
        mapInterface.goToDroneLocation();
    }

    protected int getMaxFlightPathSize() {
        return 0;
    }

    private void updateMapFragment() {

        final MapProvider mapProvider = UIUtils.getMapProvider(context);

        final FragmentManager fm = getChildFragmentManager();
        mapInterface = (MapInterface) fm.findFragmentById(R.id.map_fragment_container);
        if (mapInterface == null || mapInterface.getProvider() != mapProvider) {
            final Bundle mapArgs = new Bundle();
            mapArgs.putInt(MapInterface.EXTRA_MAX_FLIGHT_PATH_SIZE, getMaxFlightPathSize());

            mapInterface = mapProvider.getMapFragment();
            ((Fragment) mapInterface).setArguments(mapArgs);
             fm.beginTransaction().replace(R.id.map_fragment_container, (Fragment) mapInterface).commit();
        }
    }

    private List<MarkerInfo> collectMarkersFromProviders(){
        if(markerProviders.isEmpty())
            return NO_EXTERNAL_MARKERS;

        List<MarkerInfo> markers = new ArrayList<>();
        for(MapMarkerProvider provider : markerProviders){
            MarkerInfo[] externalMarkers = provider.getMapMarkers();
            Collections.addAll(markers, externalMarkers);
        }

        if(markers.isEmpty())
            return NO_EXTERNAL_MARKERS;

        return markers;
    }

    private final Runnable mUpdateMap = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null && mapInterface == null)
                return;

            final List<MarkerInfo> missionMarkerInfos = missionProxy.getMarkersInfos();
            final List<MarkerInfo> externalMarkers = collectMarkersFromProviders();

            final boolean isThereMissionMarkers = !missionMarkerInfos.isEmpty();
            final boolean isThereExternalMarkers = !externalMarkers.isEmpty();
            final boolean isHomeValid = home.isValid();
            final boolean isGuidedVisible = guided.isVisible();

            // Get the list of markers currently on the map.
            final Set<MarkerInfo> markersOnTheMap = mapInterface.getMarkerInfoList();

            if (!markersOnTheMap.isEmpty()) {
                if (isHomeValid) {
                    markersOnTheMap.remove(home);
                }

                if (isGuidedVisible) {
                    markersOnTheMap.remove(guided);
                }
                if (isThereMissionMarkers) {
                    markersOnTheMap.removeAll(missionMarkerInfos);
                }

                if(isThereExternalMarkers)
                    markersOnTheMap.removeAll(externalMarkers);

                mapInterface.removeMarkers(markersOnTheMap);
            }
            if (isHomeValid) {
                mapInterface.updateMarker(home);
            }

            if (isGuidedVisible) {
                mapInterface.updateMarker(guided);
            }
            if (isThereMissionMarkers) {
                mapInterface.updateMarkers(missionMarkerInfos, isMissionDraggable());
            }

            if(isThereExternalMarkers)
                mapInterface.updateMarkers(externalMarkers, false);

            mapInterface.updateMissionPath(missionProxy);

            mapInterface.updatePolygonsPaths(missionProxy.getPolygonsPath());

            mHandler.removeCallbacks(this);
        }
    };

    public void addMapMarkerProvider(MapMarkerProvider provider){
        if(provider != null) {
            markerProviders.add(provider);
            postUpdate();
        }
    }

    public void removeMapMarkerProvider(MapMarkerProvider provider){
        if(provider != null) {
            markerProviders.remove(provider);
            postUpdate();
        }
    }


    public interface MapMarkerProvider {
        MarkerInfo[] getMapMarkers();
    }

    public abstract boolean setAutoPanMode(AutoPanMode target);
}
