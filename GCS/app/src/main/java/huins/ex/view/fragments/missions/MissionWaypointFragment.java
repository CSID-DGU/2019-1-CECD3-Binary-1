package huins.ex.view.fragments.missions;

import android.content.Context;
import android.view.View;

import org.beyene.sius.unit.length.LengthUnit;

import huins.ex.R;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.spatial.Waypoint;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.CardTextInputView;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;
import huins.ex.view.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class MissionWaypointFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, CardTextInputView.OnTextChangeListener {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_waypoint;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.WAYPOINT));

        final NumericWheelAdapter delayAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 60, "%d s");
        CardWheelHorizontalView<Integer> delayPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .waypointDelayPicker);
        delayPicker.setViewAdapter(delayAdapter);
        delayPicker.addScrollListener(this);

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> altitudePicker = (CardWheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addScrollListener(this);

        final Waypoint item = (Waypoint) getMissionItems().get(0);
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
                final double altitude = ((LengthUnit) endValue).toBase().getValue();
                for (MissionItem item : getMissionItems()) {
                    ((Waypoint) item).getCoordinate().setAltitude(altitude);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case R.id.waypointDelayPicker:
                final int delay = (Integer) endValue;
                for (MissionItem item : getMissionItems()) {
                    ((Waypoint) item).setDelay(delay);
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
                    ((Waypoint) item).getCoordinate().setLatitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
            case R.id.waypointLongitudEditor:
                for (MissionItem item : getMissionItems()) {
                    ((Waypoint) item).getCoordinate().setLongitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
