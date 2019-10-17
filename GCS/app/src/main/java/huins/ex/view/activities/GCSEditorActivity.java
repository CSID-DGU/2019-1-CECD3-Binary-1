package huins.ex.view.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import org.beyene.sius.unit.length.LengthUnit;

import java.util.List;

import huins.ex.R;
import huins.ex.dialogs.openfile.OpenMissionDialog;
import huins.ex.dialogs.parameters.SupportEditInputDialog;
import huins.ex.interfaces.MenuOpenListener;
import huins.ex.model.MissionItemProxy;
import huins.ex.model.MissionProxy;
import huins.ex.model.MissionSelection;
import huins.ex.proto.AutoPanMode;
import huins.ex.proto.Coordinate;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.util.File.IO.MissionReader;
import huins.ex.util.analytics.GAUtils;
import huins.ex.view.activities.base.BaseUIActivity;
import huins.ex.view.dialogs.OpenFileDialog;
import huins.ex.view.fragments.editor.EditorListFragment;
import huins.ex.view.fragments.editor.EditorToolsFragment;
import huins.ex.view.fragments.editor.EditorToolsFragment.EditorTools;
import huins.ex.view.fragments.map.EditorMapFragment;
import huins.ex.view.fragments.map.GestureFragment;
import huins.ex.view.fragments.map.OnEditorInteraction;
import huins.ex.view.fragments.menu.MenuBarFragment;
import huins.ex.view.fragments.missions.MissionDetailFragment;

public class
        GCSEditorActivity extends BaseUIActivity implements GestureFragment.OnPathFinishedListener,
        EditorToolsFragment.EditorToolListener, MissionDetailFragment.OnMissionDetailListener, OnEditorInteraction, MissionSelection.OnSelectionUpdateListener, OnClickListener, OnLongClickListener, MenuOpenListener, SupportEditInputDialog.Listener {

private static final double DEFAULT_SPEED = 5;

    private static final IntentFilter eventFilter = new IntentFilter();

    private static final GCSMainActivity.GCS_MENU activity_type = GCSMainActivity.GCS_MENU.MISSION_PLANNER_EDITOR;

    public static GCSMainActivity.GCS_MENU getActivity_type() {
        return activity_type;
    }


    private static final String ITEM_DETAIL_TAG = "Item Detail Window";

    private static final String EXTRA_OPENED_MISSION_FILENAME = "extra_opened_mission_filename";
    private static final String MISSION_FILENAME_DIALOG_TAG = "Mission filename";

    //Fragment
    private EditorToolsFragment editorToolsFragment;
    private MissionDetailFragment itemDetailFragment;
    private GestureFragment gestureFragment;
    private FragmentManager fragmentManager;
    private EditorListFragment editorListFragment;

    private String openedMissionFilename;

    private MissionProxy missionProxy;

    private ImageButton itemDetailToggle;

    private TextView infoView;
    private View viewMaphelper;

    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(AttributeEvent.PARAMETERS_REFRESH_COMPLETED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    updateMissionLength();
                    break;

                case AttributeEvent.MISSION_RECEIVED:
                    final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();
                    if (planningMapFragment != null) {
                        planningMapFragment.zoomToFit();
                    }
                    break;
            }
        }
    };

    public EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    public EditorToolsFragment.EditorToolsImpl getToolImpl() {
        return editorToolsFragment.getToolImpl();
    }

    private void openMissionFile() {
        /*
        OpenFileDialog missionDialog = new OpenMissionDialog() {
            @Override
            public void waypointFileLoaded(MissionReader reader) {
                openedMissionFilename = getSelectedFilename();
                missionProxy.readMissionFromFile(reader);
                gestureMapFragment.getMapFragment().zoomToFit();
            }
        };
        missionDialog.openDialog(this);
        */
    }

    private void saveMissionFile() {
        /*
        final Context context = getApplicationContext();
        final String defaultFilename = TextUtils.isEmpty(openedMissionFilename)
                ? FileStream.getWaypointFilename("waypoints")
                : openedMissionFilename;

        final EditInputDialog dialog = EditInputDialog.newInstance(context, getString(R.string.label_enter_filename),
                defaultFilename, new EditInputDialog.Listener() {
                    @Override
                    public void onOk(CharSequence input) {
                        if (missionProxy.writeMissionToFile(input.toString())) {
                            Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT)
                                    .show();

                            final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                    .setCategory(GAUtils.Category.MISSION_PLANNING)
                                    .setAction("Mission saved to file")
                                    .setLabel("Mission items count");
                            GAUtils.sendEvent(eventBuilder);

                            return;
                        }

                        Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onCancel() {
                    }
                });

        dialog.show(getSupportFragmentManager(), "Mission filename");
        */
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_OPENED_MISSION_FILENAME, openedMissionFilename);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcseditor);
        fragmentManager = getSupportFragmentManager();

        editorToolsFragment = (EditorToolsFragment) fragmentManager.findFragmentById(R.id.editor_tools_fragment);
        editorListFragment = (EditorListFragment) fragmentManager.findFragmentById(R.id.mission_list_fragment);

        gestureFragment = ((GestureFragment) fragmentManager.findFragmentById(R.id.editor_map_fragment));
        if (gestureFragment == null) {
            gestureFragment = new GestureFragment();
            fragmentManager.beginTransaction().add(R.id.editor_map_fragment, gestureFragment).commit();
        }

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        viewMaphelper = (LinearLayout)findViewById(R.id.editor_map_btn_helper);
        final ImageButton zoomToFit = (ImageButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);

        final ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        mGoToMyLocation.setOnLongClickListener(this);

        final ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        mGoToDroneLocation.setOnLongClickListener(this);

        itemDetailToggle = (ImageButton) findViewById(R.id.toggle_action_drawer);
        itemDetailToggle.setOnClickListener(this);

        if (savedInstanceState != null) {
            openedMissionFilename = savedInstanceState.getString(EXTRA_OPENED_MISSION_FILENAME);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager.findFragmentByTag(ITEM_DETAIL_TAG);

        gestureFragment.setOnPathFinishedListener(this);
        openActionDrawer();

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();
        planningMapFragment.goToMyLocation();
    }

    @Override
    public void onClick(View v) {
        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();

        switch (v.getId()) {
            case R.id.toggle_action_drawer:
                if (missionProxy == null)
                    return;

                if (itemDetailFragment == null) {
                    List<MissionItemProxy> selected = missionProxy.selection.getSelected();
                    showItemDetail(selectMissionDetailType(selected));
                } else {
                    removeItemDetail();
                }
                break;

            case R.id.zoom_to_fit_button:
                if (planningMapFragment != null) {
                    planningMapFragment.zoomToFit();
                }
                break;

            case R.id.drone_location_button:
                planningMapFragment.goToFlightLocation();
                break;
            case R.id.my_location_button:
                planningMapFragment.goToMyLocation();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        if (missionProxy == null) return;

        EditorToolsFragment.EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onListItemClick(item);

        if (zoomToFit) {
            zoomToFitSelected();
        }
    }


    @Override
    public boolean onLongClick(View v) {
        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();

        switch (v.getId()) {
            case R.id.drone_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            case R.id.my_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            default:
                return false;
        }
    }

    //OnEditorInteraction
    @Override
    public void onMapClick(Coordinate point) {
        EditorToolsFragment.EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onMapClick(point);
    }


    @Override
    public void onListVisibilityChanged() {
    }

    @Override
    public void editorToolChanged(EditorToolsFragment.EditorTools tools) {
        setupTool();
    }

    @Override
    public void enableGestureDetection(boolean enable) {
        if (gestureFragment == null)
            return;

        if (enable)
            gestureFragment.enableGestureDetection();
        else
            gestureFragment.disableGestureDetection();
    }

    @Override
    public void skipMarkerClickEvents(boolean skip) {
        if (gestureFragment == null)
            return;

        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.skipMarkerClickEvents(skip);
    }

    @Override
    public void zoomToFitSelected() {
        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();
        List<MissionItemProxy> selected = missionProxy.selection.getSelected();
        if (selected.isEmpty()) {
            planningMapFragment.zoomToFit();
        } else {
            planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
        }
    }

    @Override
    public void openMissionFiles() {
        OpenFileDialog missionDialog = new OpenMissionDialog() {
            @Override
            public void waypointFileLoaded(MissionReader reader) {
                openedMissionFilename = getSelectedFilename();

                if(missionProxy != null) {
                    missionProxy.readMissionFromFile(reader);
                    gestureFragment.getMapFragment().zoomToFit();
                }
            }
        };
        missionDialog.openDialog(this);
    }

    @Override
    public void onPathFinished(List<Coordinate> path) {
        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();
        List<Coordinate> points = planningMapFragment.projectPathIntoMap(path);
        EditorToolsFragment.EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onPathFinished(points);
    }

    @Override
    public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        if (missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            List<MissionItemProxy>>> oldNewItemsList) {
        missionProxy.replaceAll(oldNewItemsList);
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        final boolean isEmpty = selected.isEmpty();

        if (isEmpty) {
            itemDetailToggle.setVisibility(View.GONE);
            removeItemDetail();
        } else {
            itemDetailToggle.setVisibility(View.VISIBLE);
            if (getTool() == EditorTools.SELECTOR)
                removeItemDetail();
            else {
                showItemDetail(selectMissionDetailType(selected));
            }
        }

        final EditorMapFragment planningMapFragment = gestureFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.postUpdate();
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        missionProxy = baseApp.getMissionProxy();
        if (missionProxy != null) {
            missionProxy.selection.addSelectionUpdateListener(this);
            itemDetailToggle.setVisibility(missionProxy.selection.getSelected().isEmpty() ? View.GONE : View.VISIBLE);
        }

        updateMissionLength();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();

        if (missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);

        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    private void setupTool() {
        final EditorToolsFragment.EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.setup();
        editorListFragment.enableDeleteMode(toolImpl.getEditorTools() == EditorTools.TRASH);
    }

    private void showItemDetail(MissionDetailFragment itemDetail) {
        if (itemDetailFragment == null) {
            addItemDetail(itemDetail);
        } else {
            switchItemDetail(itemDetail);
        }

        editorToolsFragment.setToolAndUpdateView(EditorTools.NONE);
    }

    private void addItemDetail(MissionDetailFragment itemDetail) {

        itemDetailFragment = itemDetail;
        if (itemDetailFragment == null)
            return;

        fragmentManager.beginTransaction()
                .replace(getActionDrawerId(), itemDetailFragment, ITEM_DETAIL_TAG)
                .commit();
        updateLocationButtonsMargin(true);

    }

    public void switchItemDetail(MissionDetailFragment itemDetail) {
        removeItemDetail();
        addItemDetail(itemDetail);
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null) {
            fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            itemDetailFragment = null;

            updateLocationButtonsMargin(false);
        }
    }

    private void updateLocationButtonsMargin(boolean isOpened) {
        final View actionDrawer = getActionDrawer();
        if (actionDrawer == null)
            return;

        itemDetailToggle.setActivated(isOpened);

        // Update the right margin for the my location button
        final ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) viewMaphelper
                .getLayoutParams();
        final int rightMargin = isOpened ? marginLp.leftMargin + actionDrawer.getWidth() : marginLp.leftMargin;
        marginLp.setMargins(marginLp.leftMargin, marginLp.topMargin, rightMargin, marginLp.bottomMargin);
        viewMaphelper.requestLayout();
    }

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies) {
        if (proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for (MissionItemProxy proxy : proxies) {
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if (referenceType == null) {
                referenceType = proxyType;
            } else if (referenceType != proxyType
                    || MissionDetailFragment.typeWithNoMultiEditSupport.contains(referenceType)) {
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    private void updateMissionLength() {
        if (missionProxy != null) {

            double missionLength = missionProxy.getMissionLength();
            LengthUnit convertedMissionLength = unitSystem.getLengthUnitProvider().boxBaseValueToTarget(missionLength);
            double speedParameter = baseApp.getFlight().getSpeedParameter() / 100; //cm/s to m/s conversion.
            if(speedParameter == 0)
                speedParameter = DEFAULT_SPEED;

            int time = (int) (missionLength / speedParameter);

            String infoString = getString(R.string.editor_info_window_distance, convertedMissionLength.toString())
                    + ", " + getString(R.string.editor_info_window_flight_time, time / 60, time % 60);

            infoView.setText(infoString);
            // Remove detail window if item is removed
            if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                removeItemDetail();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setInfoViewMargin();
    }

    private void setInfoViewMargin() {
//        final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 67, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams textParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParam.topMargin = getMenuBarViewHeight();
        infoView.setLayoutParams(textParam);
        infoView.requestLayout();
    }
    private int getMenuBarViewHeight() {
        MenuBarFragment menuBarFragment = (MenuBarFragment) fragmentManager.findFragmentById(R.id.fragment_menubar);
        LinearLayout ll = (LinearLayout) menuBarFragment.getView().findViewById(R.id.MENU_EASYBTN);
        return ll.getHeight();
    }

    @Override
    public void onMenuOpened() {
    }
    @Override
    public void onMenuClosed() {
    }

    @Override
    public void onOk(String dialogTag, CharSequence input) {
        final Context context = getApplicationContext();

        switch (dialogTag) {
            case MISSION_FILENAME_DIALOG_TAG:
                if (missionProxy.writeMissionToFile(input.toString())) {
                    Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT)
                            .show();

                    final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                            .setCategory(GAUtils.Category.MISSION_PLANNING)
                            .setAction("Mission saved to file")
                            .setLabel("Mission items count");
                    GAUtils.sendEvent(eventBuilder);

                    break;
                }

                Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onCancel(String dialogTag) {

    }
}
