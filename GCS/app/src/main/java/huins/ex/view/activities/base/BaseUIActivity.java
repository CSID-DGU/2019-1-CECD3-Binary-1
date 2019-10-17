package huins.ex.view.activities.base;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import huins.ex.R;
//import huins.ex.interfaces.MenuInterfaceListener;
import huins.ex.model.MissionProxy;
import huins.ex.proto.Flight;
import huins.ex.util.constants.ConstantsNet;
import huins.ex.util.constants.GCSPreferences;
import huins.ex.util.log.GCSLogger;
import huins.ex.util.unit.UnitManager;
import huins.ex.util.unit.systems.UnitSystem;
import huins.ex.view.activities.GCSBaseApp;
import huins.ex.view.activities.GCSMainActivity;
import huins.ex.view.widgets.SlidingDrawer;

/**
 * Created by suhak on 15. 6. 15.
 */
public class BaseUIActivity extends AppCompatActivity implements GCSBaseApp.GCSApiListener, SlidingDrawer.OnDrawerOpenListener, SlidingDrawer.OnDrawerCloseListener {

    private final static String TAG = BaseUIActivity.class.getSimpleName();

    private LocalBroadcastManager localbroadcastmanager;
    //private ScreenOrientation screenOrientation = new ScreenOrientation(this);
    private static final IntentFilter superIntentFilter = new IntentFilter();

    protected GCSPreferences GCSPrefs;
    protected UnitSystem unitSystem;

    protected GCSBaseApp baseApp;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private FrameLayout contentLayout;

    private DisplayMetrics metrics;
    //UI
    private SlidingDrawer actionDrawer;

    protected LocalBroadcastManager getBroadcastManager() {
        return localbroadcastmanager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();

        baseApp = (GCSBaseApp) getApplication();

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_navigation_ui, null);
        contentLayout = (FrameLayout) mDrawerLayout.findViewById(R.id.content_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        actionDrawer = (SlidingDrawer) mDrawerLayout.findViewById(R.id.action_drawer_container);

            GCSPrefs = new GCSPreferences(context);
        unitSystem = UnitManager.getUnitSystem(context);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        localbroadcastmanager = LocalBroadcastManager.getInstance(context);

      //  toggleFlightConnection();

        actionDrawer = (SlidingDrawer) mDrawerLayout.findViewById(R.id.action_drawer_container);
        actionDrawer.setOnDrawerCloseListener(this);
        actionDrawer.setOnDrawerOpenListener(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        final View contentView = getLayoutInflater().inflate(layoutResID, mDrawerLayout, false);
        contentLayout.addView(contentView);
        setContentView(mDrawerLayout);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localbroadcastmanager = null;
    }

    @Override
    public void onApiConnected() {
        invalidateOptionsMenu();
        // skku 이시점에 알려야 하는지 확인필요 필요없으면 삭제
        localbroadcastmanager.sendBroadcast(new Intent(MissionProxy.ACTION_MISSION_PROXY_UPDATE));
    }

    @Override
    public void onApiDisconnected() {
    }

    protected int getDisplayWidth()
    {
        return metrics.widthPixels;
    }

    protected  int getDisplayheight()
    {
        return metrics.heightPixels;
    }

    protected View getActionDrawer() {
        return actionDrawer;
    }
    
    protected int getActionDrawerId() {
        return R.id.action_drawer_content;
    }

    protected boolean isActionDrawerOpened() {
        return actionDrawer.isOpened();
    }

    /**
     * Called when the action drawer is opened.
     * Should be override by children as needed.
     */
    @Override
    public void onDrawerOpened() {
    }

    /**
     * Called when the action drawer is closed.
     * Should be override by children as needed.
     */
    @Override
    public void onDrawerClosed() {
    }

    protected void openActionDrawer() {
        actionDrawer.animateOpen();
        actionDrawer.lock();
    }

    protected void closeActionDrawer() {
        actionDrawer.animateClose();
        actionDrawer.lock();
    }
    @Override
    protected void onStart() {
        super.onStart();
        GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, TAG, "onStart");
        unitSystem = UnitManager.getUnitSystem(getApplicationContext());
        baseApp.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        baseApp.removeApiListener(this);
    }


    static {
        superIntentFilter.addAction(ConstantsNet.GCS_STATE_CONNECTED);
        superIntentFilter.addAction(ConstantsNet.GCS_STATE_DISCONNECTED);
    }

    public void toggleFlightConnection() {
        final Flight flight = baseApp.getFlight();

        if (flight != null && flight.IsConnected())
            baseApp.disconnectFromFlight();
        else
            baseApp.connectToFlight();
    }
}
