package huins.ex.view.fragments.missions;

import android.content.Context;
import android.view.View;

import org.beyene.sius.unit.length.LengthUnit;

import java.util.List;

import huins.ex.R;
import huins.ex.model.MissionProxy;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.spatial.Circle;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.CardTextInputView;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;
import huins.ex.view.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class MissionCircleFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, CardTextInputView.OnTextChangeListener {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_circle;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CIRCLE));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();

        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));

        CardWheelHorizontalView<LengthUnit> altitudePicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addScrollListener(this);

        final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 10, "%d");

        CardWheelHorizontalView<Integer> loiterTurnPicker = (CardWheelHorizontalView<Integer>) view
                .findViewById(R.id.loiterTurnPicker);
        loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
        loiterTurnPicker.addScrollListener(this);

        final LengthWheelAdapter loiterRadiusAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(0), lengthUP.boxBaseValueToTarget(50));
        CardWheelHorizontalView<LengthUnit> loiterRadiusPicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.loiterRadiusPicker);
        loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
        loiterRadiusPicker.addScrollListener(this);

        // Use the first one as reference.
        final Circle firstItem = getMissionItems().get(0);
        altitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getCoordinate().getAltitude()));
        loiterTurnPicker.setCurrentValue(firstItem.getTurns());
        loiterRadiusPicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getRadius()));


        CardTextInputView laditudePicker = (CardTextInputView) view.findViewById(R.id.waypointLatitudeEditor);
        CardTextInputView longitudePicker = (CardTextInputView) view.findViewById(R.id.waypointLongitudEditor);

        laditudePicker.addEditListener(this);
        longitudePicker.addEditListener(this);

        laditudePicker.setCurrentValue(firstItem.getCoordinate().getLatitude());
        longitudePicker.setCurrentValue(firstItem.getCoordinate().getLongitude());
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Object startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Object oldValue, Object newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, Object startValue, Object endValue) {
        switch (cardWheel.getId()) {
            case R.id.altitudePicker: {
                final double baseValue = ((LengthUnit) endValue).toBase().getValue();
                for (Circle item : getMissionItems()) {
                    item.getCoordinate().setAltitude(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
            }

            case R.id.loiterRadiusPicker: {
                final double baseValue =  ((LengthUnit) endValue).toBase().getValue();
                for (Circle item : getMissionItems()) {
                    item.setRadius(baseValue);
                }

                MissionProxy missionProxy = getMissionProxy();
                if (missionProxy != null)
                    missionProxy.notifyMissionUpdate();
                break;
            }

            case R.id.loiterTurnPicker:
                int turns = (Integer) endValue;
                for (Circle item : getMissionItems()) {
                    item.setTurns(turns);
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
                    ((Circle) item).getCoordinate().setLatitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
            case R.id.waypointLongitudEditor:
                for (MissionItem item : getMissionItems()) {
                    ((Circle) item).getCoordinate().setLongitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }

    @Override
    public List<Circle> getMissionItems() {
        return (List<Circle>) super.getMissionItems();
    }
}
