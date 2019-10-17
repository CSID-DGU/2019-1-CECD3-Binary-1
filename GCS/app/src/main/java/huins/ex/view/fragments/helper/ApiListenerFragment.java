package huins.ex.view.fragments.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import huins.ex.model.MissionProxy;
import huins.ex.proto.Flight;
import huins.ex.util.constants.GCSPreferences;
import huins.ex.util.unit.UnitManager;
import huins.ex.util.unit.providers.area.AreaUnitProvider;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.util.unit.providers.speed.SpeedUnitProvider;
import huins.ex.util.unit.systems.UnitSystem;
import huins.ex.view.activities.GCSBaseApp;


/**
 * Created by suhak on 15. 6. 29.
 */
@SuppressLint("ValidFragment")
public abstract class ApiListenerFragment extends Fragment implements GCSBaseApp.GCSApiListener {


    private GCSBaseApp baseApp;
    private LocalBroadcastManager localBroadcastManager;


//    private static final IntentFilter filter = new IntentFilter();
//
//    static {
//        filter.addAction(SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE);
//    }
//
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            switch (intent.getAction()) {
//            case SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE:
//            setupUnitProviders(context);
//            break;
//            }
//        }
//    };

    private LengthUnitProvider lengthUnitProvider;
    private AreaUnitProvider areaUnitProvider;
    private SpeedUnitProvider speedUnitProvider;

    protected MissionProxy getMissionProxy() { return baseApp.getMissionProxy(); }

    protected GCSPreferences getAppPrefs() {
        return baseApp.getAppPreferences();
    }

    protected Flight getFlight() {
        return baseApp.getFlight();
    }

    protected LocalBroadcastManager getBroadcastManager() {
        return localBroadcastManager;
    }

    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        baseApp = (GCSBaseApp) activity.getApplication();

        final Context context = activity.getApplicationContext();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        setupUnitProviders(context);
    }

    @Override
    public void onStart() {
        super.onStart();

        setupUnitProviders(getContext());
//        localBroadcastManager.registerReceiver(receiver, filter);

        baseApp.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        baseApp.removeApiListener(this);

//        localBroadcastManager.unregisterReceiver(receiver);
    }

        protected LengthUnitProvider getLengthUnitProvider(){
                return lengthUnitProvider;
                }

        protected AreaUnitProvider getAreaUnitProvider(){
                return areaUnitProvider;
                }

        protected SpeedUnitProvider getSpeedUnitProvider(){
                return speedUnitProvider;
                }

    private void setupUnitProviders(Context context) {
        if (context == null)
            return;

    final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
            lengthUnitProvider = unitSystem.getLengthUnitProvider();
            areaUnitProvider = unitSystem.getAreaUnitProvider();
            speedUnitProvider = unitSystem.getSpeedUnitProvider();

    }
}
