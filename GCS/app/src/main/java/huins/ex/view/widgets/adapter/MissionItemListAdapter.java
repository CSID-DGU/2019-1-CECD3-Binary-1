package huins.ex.view.widgets.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.beyene.sius.unit.length.LengthUnit;

import huins.ex.R;
import huins.ex.model.MissionItemProxy;
import huins.ex.model.MissionProxy;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.command.CameraTrigger;
import huins.ex.proto.mission.item.command.ChangeSpeed;
import huins.ex.proto.mission.item.command.EpmGripper;
import huins.ex.proto.mission.item.command.ReturnToLaunch;
import huins.ex.proto.mission.item.command.SetServo;
import huins.ex.proto.mission.item.command.Takeoff;
import huins.ex.proto.mission.item.command.YawCondition;
import huins.ex.proto.mission.item.complex.Survey;
import huins.ex.proto.mission.item.spatial.Circle;
import huins.ex.proto.mission.item.spatial.Land;
import huins.ex.proto.mission.item.spatial.RegionOfInterest;
import huins.ex.proto.mission.item.spatial.SplineWaypoint;
import huins.ex.util.unit.UnitManager;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.fragments.map.OnEditorInteraction;
import huins.ex.view.widgets.ReorderRecyclerView;

/**
 * Created by fhuya on 12/9/14.
 */
public class MissionItemListAdapter extends ReorderRecyclerView.ReorderAdapter<MissionItemListAdapter.ViewHolder> {

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View viewContainer;
        final TextView nameView;
        final TextView altitudeView;

        public ViewHolder(View container, TextView nameView, TextView altitudeView) {
            super(container);
            this.viewContainer = container;
            this.nameView = nameView;
            this.altitudeView = altitudeView;
        }

    }

    private final MissionProxy missionProxy;
    private final OnEditorInteraction editorListener;
    private final LengthUnitProvider lengthUnitProvider;

    public MissionItemListAdapter(Context context, MissionProxy missionProxy, OnEditorInteraction editorListener) {
        this.missionProxy = missionProxy;
        this.editorListener = editorListener;
        this.lengthUnitProvider = UnitManager.getUnitSystem(context).getLengthUnitProvider();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position){
        return missionProxy.getItems().get(position).getStableId();
    }

    @Override
    public int getItemCount() {
        return missionProxy.getItems().size();
    }

    @Override
    public void swapElements(int fromIndex, int toIndex) {
        missionProxy.swap(fromIndex, toIndex);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_editor_list_item, parent, false);

        final TextView nameView = (TextView) view.findViewById(R.id.rowNameView);
        final TextView altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);

        return new ViewHolder(view, nameView, altitudeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final MissionItemProxy proxy = missionProxy.getItems().get(position);

        final View container = viewHolder.viewContainer;
        container.setActivated(missionProxy.selection.selectionContains(proxy));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editorListener != null)
                editorListener.onItemClick(proxy, true);
            }
        });

        final TextView nameView = viewHolder.nameView;
        final TextView altitudeView = viewHolder.altitudeView;

        final MissionProxy missionProxy = proxy.getMission();
        final MissionItem missionItem = proxy.getMissionItem();

        nameView.setText(String.format("%3d", missionProxy.getOrder(proxy)));

        int leftDrawable;

        // Spatial item's icons
        if (missionItem instanceof MissionItem.SpatialItem) {
            if (missionItem instanceof SplineWaypoint) {
                leftDrawable = R.drawable.ic_mission_spline_wp;
            } else if (missionItem instanceof Circle) {
                leftDrawable = R.drawable.ic_mission_circle_wp;
            } else if (missionItem instanceof RegionOfInterest) {
                leftDrawable = R.drawable.ic_mission_roi_wp;
            } else if (missionItem instanceof Land) {
                leftDrawable = R.drawable.ic_mission_land_wp;
            } else {
                leftDrawable = R.drawable.ic_mission_wp;
            }
        // Command icons
        } else if (missionItem instanceof MissionItem.Command) {
            if (missionItem instanceof CameraTrigger) {
                leftDrawable = R.drawable.ic_mission_camera_trigger_wp;
            } else if (missionItem instanceof ChangeSpeed) {
                leftDrawable = R.drawable.ic_mission_change_speed_wp;
            } else if (missionItem instanceof EpmGripper) {
                leftDrawable = R.drawable.ic_mission_epm_gripper_wp;
            } else if (missionItem instanceof ReturnToLaunch) {
                leftDrawable = R.drawable.ic_mission_rtl_wp;
            } else if (missionItem instanceof SetServo) {
                leftDrawable = R.drawable.ic_mission_set_servo_wp;
            } else if (missionItem instanceof Takeoff) {
                leftDrawable = R.drawable.ic_mission_takeoff_wp;
            } else if (missionItem instanceof YawCondition) {
                leftDrawable = R.drawable.ic_mission_yaw_cond_wp;
            } else {
                leftDrawable = R.drawable.ic_mission_command_wp;
            }
        // Complex item's icons
        // TODO CameraDetail (inconvertible type) and StructureScanner (condition always false) WPs
        } else if (missionItem instanceof MissionItem.ComplexItem) {
            if (missionItem instanceof Survey) {
                leftDrawable = R.drawable.ic_mission_survey_wp;
            } else {
                leftDrawable = R.drawable.ic_mission_command_wp;
            }
        // Fallback icon
        } else {
            leftDrawable = R.drawable.ic_mission_wp;
        }

        altitudeView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);

        if (missionItem instanceof MissionItem.SpatialItem) {
            MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) missionItem;
            double altitude = waypoint.getCoordinate().getAltitude();
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            LengthUnit roundedConvertedAltitude = (LengthUnit) convertedAltitude.valueOf(Math.round(convertedAltitude.getValue()));
            altitudeView.setText(roundedConvertedAltitude.toString());

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

            try {
                double diff = missionProxy.getAltitudeDiffFromPreviousItem(proxy);
                if (diff > 0) {
                    altitudeView.setTextColor(Color.RED);
                } else if (diff < 0) {
                    altitudeView.setTextColor(Color.BLUE);
                }
            } catch (Exception e) {
                // Do nothing when last item doesn't have an altitude
            }
        } else if (missionItem instanceof Survey) {
            double altitude = ((Survey) missionItem).getSurveyDetail().getAltitude();
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            LengthUnit roundedConvertedAltitude = (LengthUnit) convertedAltitude.valueOf(Math.round(convertedAltitude.getValue()));
            altitudeView.setText(roundedConvertedAltitude.toString());

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

        } else if (missionItem instanceof Takeoff) {
            double altitude = ((Takeoff) missionItem).getTakeoffAltitude();
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            LengthUnit roundedConvertedAltitude = (LengthUnit) convertedAltitude.valueOf(Math.round(convertedAltitude.getValue()));
            altitudeView.setText(roundedConvertedAltitude.toString());

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);
        } else {
            altitudeView.setText("");
        }
    }
}
