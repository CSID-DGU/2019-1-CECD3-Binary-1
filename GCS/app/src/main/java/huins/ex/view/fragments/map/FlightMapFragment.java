package huins.ex.view.fragments.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import huins.ex.R;
import huins.ex.interfaces.MarkerInfo;
import huins.ex.proto.AutoPanMode;
import huins.ex.proto.Coordinate;
import huins.ex.proto.MapInterface;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Gps;
import huins.ex.proto.property.GuidedState;
import huins.ex.proto.property.State;
import huins.ex.util.FlightLocationHelper;
import huins.ex.view.dialogs.GuidedDialog;

/**
 * Created by suhak on 15. 7. 13.
 */
public class FlightMapFragment extends FlightMap implements MapInterface.OnMapLongClickListener,
        MapInterface.OnMarkerClickListener, MapInterface.OnMarkerDragListener, GuidedDialog.GuidedDialogListener {

    private static final int MAX_TOASTS_FOR_LOCATION_PRESS = 3;

    private static final String PREF_USER_LOCATION_FIRST_PRESS = "pref_user_location_first_press";
    private static final int DEFAULT_USER_LOCATION_FIRST_PRESS = 0;

    private static final String PREF_DRONE_LOCATION_FIRST_PRESS = "pref_drone_location_first_press";
    private static final int DEFAULT_DRONE_LOCATION_FIRST_PRESS = 0;

    private static boolean didZoomOnUserLocation = false;


    public interface OnGuidedClickListener {
        void onGuidedClick(Coordinate coord);
    }

    private static final IntentFilter eventFilter = new IntentFilter(AttributeEvent.STATE_ARMING);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (AttributeEvent.STATE_ARMING.equals(action)) {
                final State droneState = flight.getAttribute(AttributeType.STATE);
                if (droneState.isArmed()) {
                    mapInterface.clearFlightPath();
                }
            }
        }
    };

    private OnGuidedClickListener guidedClickListener;

    public void setGuidedClickListener(OnGuidedClickListener guidedClickListener) {
        this.guidedClickListener = guidedClickListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(inflater, viewGroup, bundle);

        mapInterface.setOnMapLongClickListener(this);
        mapInterface.setOnMarkerDragListener(this);
        mapInterface.setOnMarkerClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapInterface.selectAutoPanMode(GCSPrefs.getAutoPanMode());

        if (!didZoomOnUserLocation) {
            super.goToMyLocation();
            didZoomOnUserLocation = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapInterface.selectAutoPanMode(AutoPanMode.DISABLED);
    }

    @Override
    protected int getMaxFlightPathSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString("pref_max_flight_path_size", "0"));
    }

    @Override
    public boolean setAutoPanMode(AutoPanMode target) {
        // Update the map panning preferences.
        GCSPrefs.setAutoPanMode(target);
        mapInterface.selectAutoPanMode(target);
        return true;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onMapLongClick(Coordinate coord) {
        if (flight != null && flight.IsConnected()) {
            final GuidedState guidedState = flight.getAttribute(AttributeType.GUIDED_STATE);
            if (guidedState.isInitialized()) {
                if(guidedClickListener != null)
                    guidedClickListener.onGuidedClick(coord);
            } else {
                GuidedDialog dialog = new GuidedDialog();
                dialog.setCoord(FlightLocationHelper.CoordToLatLang(coord));
                dialog.setListener(this);
                dialog.show(getChildFragmentManager(), "GUIDED dialog");
            }

        }
    }

    @Override
    public void onForcedGuidedPoint(LatLng coord) {
        try {
            flight.sendGuidedPoint(FlightLocationHelper.LatLngToCoord(coord), true);
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMarkerDragStart(MarkerInfo markerInfo) {
    }

    @Override
    public void onMarkerDrag(MarkerInfo markerInfo) {
    }

    @Override
    public void onMarkerDragEnd(MarkerInfo markerInfo) {
        flight.sendGuidedPoint(markerInfo.getPosition(), false);
    }

    @Override
    public boolean onMarkerClick(MarkerInfo markerInfo) {
        if(markerInfo == null)
            return false;

        flight.sendGuidedPoint(markerInfo.getPosition(), false);
        return true;
    }

    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

    @Override
    public void goToMyLocation() {
        super.goToMyLocation();
        int pressCount = GCSPrefs.prefs.getInt(PREF_USER_LOCATION_FIRST_PRESS,
                DEFAULT_USER_LOCATION_FIRST_PRESS);
        if (pressCount < MAX_TOASTS_FOR_LOCATION_PRESS) {
            Toast.makeText(context, R.string.user_autopan_long_press, Toast.LENGTH_LONG).show();
            GCSPrefs.prefs.edit().putInt(PREF_USER_LOCATION_FIRST_PRESS, pressCount + 1).apply();
        }
    }

    @Override
    public void goToFlightLocation() {
        super.goToFlightLocation();

        if(this.flight == null)
            return;

        final Gps flightGps = this.flight.getAttribute(AttributeType.GPS);
        if (flightGps == null || !flightGps.isValid())
            return;

        final int pressCount = GCSPrefs.prefs.getInt(PREF_DRONE_LOCATION_FIRST_PRESS,
                DEFAULT_DRONE_LOCATION_FIRST_PRESS);
        if (pressCount < MAX_TOASTS_FOR_LOCATION_PRESS) {
            Toast.makeText(context, R.string.drone_autopan_long_press, Toast.LENGTH_LONG).show();
            GCSPrefs.prefs.edit().putInt(PREF_DRONE_LOCATION_FIRST_PRESS, pressCount + 1).apply();
        }
    }

}
