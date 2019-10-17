package huins.ex.view.fragments.missions;

import android.content.Context;
import android.view.View;

import org.beyene.sius.unit.length.LengthUnit;

import huins.ex.R;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.spatial.SplineWaypoint;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.CardTextInputView;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;
import huins.ex.view.widgets.spinnerWheel.adapters.NumericWheelAdapter;

/**
 * This class renders the detail view for a spline waypoint mission item.
 */
public class MissionSplineWaypointFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, CardTextInputView.OnTextChangeListener {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_spline_waypoint;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SPLINE_WAYPOINT));

        final NumericWheelAdapter delayAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0,
                60, "%d s");
        CardWheelHorizontalView<Integer> delayPicker = (CardWheelHorizontalView<Integer>) view.findViewById(R.id
                .waypointDelayPicker);
        delayPicker.setViewAdapter(delayAdapter);
        delayPicker.addScrollListener(this);

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> altitudePicker = (CardWheelHorizontalView<LengthUnit>) view.findViewById
                (R.id.altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addScrollListener(this);

        SplineWaypoint item = (SplineWaypoint) getMissionItems().get(0);
        delayPicker.setCurrentValue((int) item.getDelay());
        altitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(item.getCoordinate().getAltitude()));


        CardTextInputView laditudePicker = (CardTextInputView) view.findViewById(R.id.waypointLatitudeEditor);
        CardTextInputView longitudePicker = (CardTextInputView) view.findViewById(R.id.waypointLongitudEditor);

        laditudePicker.addEditListener(this);
        longitudePicker.addEditListener(this);

        laditudePicker.setCurrentValue(item.getCoordinate().getLatitude());
        longitudePicker.setCurrentValue(item.getCoordinate().getLongitude());
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Object startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Object oldValue, Object newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, Object startValue, Object endValue) {
        switch (wheel.getId()) {
            case R.id.altitudePicker:
                final double baseValue = ((LengthUnit) endValue).toBase().getValue();
                for (MissionItem item : getMissionItems()) {
                    ((SplineWaypoint) item).getCoordinate().setAltitude(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case R.id.waypointDelayPicker:
                final int delay = (Integer) endValue;
                for (MissionItem item : getMissionItems()) {
                    ((SplineWaypoint) item).setDelay(delay);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }

    @Override
    public void onTextEditStarted(CardTextInputView view, double value) {

    }

    @Override
    public void onTextEditEnded(CardTextInputView view, double value) {
        switch (view.getId()) {
            case R.id.waypointLatitudeEditor:
                for (MissionItem item : getMissionItems()) {
                    ((SplineWaypoint) item).getCoordinate().setLatitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
            case R.id.waypointLongitudEditor:
                for (MissionItem item : getMissionItems()) {
                    ((SplineWaypoint) item).getCoordinate().setLongitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
