package huins.ex.view.fragments.menu;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.util.List;

import huins.ex.R;
import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;
import huins.ex.proto.action.GimbalAction;
import huins.ex.proto.action.RCAction;
import huins.ex.proto.action.StreamActions;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.property.Altitude;
import huins.ex.proto.property.Battery;
import huins.ex.proto.property.State;
import huins.ex.proto.property.Type;
import huins.ex.proto.property.VehicleMode;
import huins.ex.util.Utils;
import huins.ex.util.analytics.GAUtils;
import huins.ex.util.constants.ConstantsBase;
import huins.ex.util.constants.EmergencyModeType;
import huins.ex.view.dialogs.SlideToUnlockDialog;
import huins.ex.view.fragments.helper.ApiListenerFragment;
import huins.ex.view.widgets.spinners.ModeAdapter;
import huins.ex.view.widgets.spinners.SpinnerSelfSelect;

/**
 * Created by suhak on 15. 7. 22.
 */
public class MenuBarFragment extends ApiListenerFragment implements OnClickListener {

    private final double MAX_ALTITUDE = 121.92; //meters
    private final double low_battery_level = 10.0; //%

    private ImageButton _btn_rtl;
    private ImageButton _btn_takeoff;
    private ImageButton _btn_arm;
    private ImageButton _btn_joystick;

    private SpinnerSelfSelect _flightMode;
    private ModeAdapter modeAdapter;
    private int lastFlightType = -1;

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.STATE_ARMING);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
        eventFilter.addAction(AttributeEvent.BATTERY_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //final Flight flight = getFlight();
            switch (action) {
                case AttributeEvent.STATE_VEHICLE_MODE:
                    UpateVihicleMode();
                    //Toast.makeText(getContext(), "STATE_VEHICLE_MODE!", Toast.LENGTH_SHORT).show();
                    break;
                case AttributeEvent.TYPE_UPDATED:
                    updateFlightMode();
                    //Toast.makeText(getContext(), "TYPE_UPDATED!", Toast.LENGTH_SHORT).show();
                    break;
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                    updateFlightMode();
                    break;
                case AttributeEvent.STATE_ARMING:
                    Update_arming();
                /*{
                    final State flightState = flight.getAttribute(AttributeType.STATE);
                    if (flightState.isArmed()) {

                        UIupdate_arm();
                    } else {

                        UIupdate_disarm();
                    }
                }
                */
                    break;
                case AttributeEvent.STATE_UPDATED:
                {
                    //final State flightState = flight.getAttribute(AttributeType.STATE);
                    /*
                    if (flightState.isFlying())
                        Toast.makeText(gcetContext(), "drone is flying!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), "drone is NOT flying!", Toast.LENGTH_SHORT).show();
                        */
                }
                    // enableSlidingUpPanel(baseApp.getFlight());
                    break;
                case AttributeEvent.BATTERY_UPDATED:
                    final Battery flightBattery = getFlight().getAttribute(AttributeType.BATTERY);
                    if (flightBattery != null && flightBattery.getBatteryRemain() <= low_battery_level)
//                        setEmergencyMode(EmergencyModeType.BATTERY_LOW_LEVEL);
                    break;
                case AttributeEvent.ALTITUDE_UPDATED:
                    final Altitude altitude = getFlight().getAttribute(AttributeType.ALTITUDE);
                    if (altitude != null && altitude.getAltitude() > MAX_ALTITUDE)
//                        setEmergencyMode(EmergencyModeType.EXEED_MAX_ALTITUDE);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menubar, container, false);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _btn_rtl = (ImageButton) view.findViewById(R.id.MENU_RTL);
        _btn_takeoff = (ImageButton) view.findViewById(R.id.MENU_TAKEOFF);
        _btn_arm = (ImageButton) view.findViewById(R.id.MENU_ARM);
        _btn_joystick = (ImageButton) view.findViewById(R.id.MENU_JOYSTICK);

        _flightMode = (SpinnerSelfSelect) view.findViewById(R.id.menubar_flight_mode);
        modeAdapter = new ModeAdapter(getContext(), R.layout.spinner_drop_down_flight_mode);

        _btn_rtl.setOnClickListener(this);
        _btn_takeoff.setOnClickListener(this);
        _btn_arm.setOnClickListener(this);
        _btn_joystick.setOnClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

//        try {
//            menuinterfacelistener = (MenuInterfaceListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement menuinterfacelistener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private void getTakeOffConfirmation(){
        final SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("take off", new Runnable() {
            @Override
            public void run() {
                getFlight().doGuidedTakeoff(ConstantsBase.TAKEOFF_ALTITUDE_LOW);
            }
        });
        unlockDialog.show(getChildFragmentManager(), "Slide to take off");
    }


    private void getArmingConfirmation() {
        SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("arm", new Runnable() {
            @Override
            public void run() {
                getFlight().arm(true);
            }
        }) ;
        unlockDialog.show(getChildFragmentManager(), "Slide To Arm");
    }

    private void UpateVihicleMode(){
        State flightState = getFlight().getAttribute(AttributeType.STATE);
        if (flightState == null)
            return;

        final VehicleMode flightMode = flightState.getVehicleMode();
        if (flightMode == null)
            return;

        switch (flightMode) {

            case COPTER_RTL:
                break;

            case COPTER_LAND:
                UIupdate_land();
                break;
            default:
                break;
        }
    }


    private void Update_arming()
    {
        final Flight flight = getFlight();
        final State flightState = flight.getAttribute(AttributeType.STATE);
        if (flightState != null && flightState.isArmed()) {

            UIupdate_arm();
        } else {

            UIupdate_disarm();
        }
    }

    private void UIupdate_arm()
    {
        _btn_arm.setBackgroundResource(R.drawable.fc_t_11_2);
        _btn_takeoff.setEnabled(true);
    }

    private void UIupdate_disarm()
    {
        _btn_arm.setBackgroundResource(R.drawable.fc_t_11);
        _btn_takeoff.setEnabled(false);
    }

    private void UIupdate_takeoff()
    {
        _btn_takeoff.setBackgroundResource(R.drawable.fc_t_3_2);
    }

    private void UIupdate_land()
    {
        _btn_takeoff.setBackgroundResource(R.drawable.fc_t_3_1);
    }

    private boolean is_takeoff = false;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.MENU_TAKEOFF:
                if (is_takeoff) {
                    getFlight().changeVehicleMode(VehicleMode.COPTER_LAND);
                    //UIupdate_land();
                } else {
                    getTakeOffConfirmation();
                    UIupdate_takeoff();
                }
                is_takeoff = !is_takeoff;
                break;
            case R.id.MENU_RTL:
                getFlight().changeVehicleMode(VehicleMode.COPTER_RTL);
                break;
            case R.id.MENU_ARM:
                final State flightState = getFlight().getAttribute(AttributeType.STATE);
                if(flightState == null) {
                    return;
                }
                if(flightState.isArmed()) {
                    getFlight().arm(false);
                } else {
                    getArmingConfirmation();
                    //UIupdate_disarm();
                }
                break;
            case R.id.MENU_JOYSTICK:
                if(!getFlight().IsConnected()) {
                    Toast.makeText(this.getActivity().getApplicationContext(), "Flight not connected", Toast.LENGTH_LONG).show();
                    return;
                }
                if(rc_usable) {
                    rc_usable = false;
                    getFlight().performAction(new Action(RCAction.ACTION_RC_CTR_END));
                } else {
                    rc_usable = true;
                    getFlight().performAction(new Action(RCAction.ACTION_RC_CTR_START));
                }
                break;
            default:
                break;
        }
    }
    private boolean rc_usable = false;
    @Override
    public void onApiConnected() {

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
        _flightMode.setAdapter(modeAdapter);
        _flightMode.setOnSpinnerItemSelectedListener(new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
            @Override
            public void onSpinnerItemSelected(Spinner spinner, int position) {
                final Flight flight = getFlight();
                if (flight.IsConnected()) {
                    final VehicleMode newMode = (VehicleMode) spinner.getItemAtPosition(position);
                    flight.changeVehicleMode(newMode);

                    //Record the attempt to change flight modes
                    final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                            .setCategory(GAUtils.Category.FLIGHT)
                            .setAction("Flight mode changed")
                            .setLabel(newMode.getLabel());
                    GAUtils.sendEvent(eventBuilder);
                }
            }
        });
        updateFlightMode();

        UpateVihicleMode();

        Update_arming();
    }

    private void updateFlightMode() {
        final Flight flight = getFlight();

        final boolean isDroneConnected = flight.IsConnected();
        final int flightType;
        if (isDroneConnected) {
            Type type = flight.getAttribute(AttributeType.TYPE);
            flightType = type.getDroneType();
        } else {
            flightType = -1;
        }

        if (flightType != lastFlightType) {
            final List<VehicleMode> flightModes = VehicleMode.getVehicleModePerDroneType(flightType);

            modeAdapter.clear();
            modeAdapter.addAll(flightModes);
            modeAdapter.notifyDataSetChanged();

            lastFlightType = flightType;
        }

        if (isDroneConnected) {
            final State droneState = flight.getAttribute(AttributeType.STATE);
            _flightMode.forcedSetSelection(modeAdapter.getPosition(droneState.getVehicleMode()));
        }
    }
    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }
}
