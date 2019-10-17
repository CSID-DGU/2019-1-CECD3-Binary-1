package huins.ex.view.fragments.missions;

import org.beyene.sius.unit.length.LengthUnit;

import huins.ex.R;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.command.Takeoff;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;

public class MissionTakeoffFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_takeoff;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.TAKEOFF));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(0), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> cardAltitudePicker = (CardWheelHorizontalView) getView()
                .findViewById(R.id.altitudePicker);
        cardAltitudePicker.setViewAdapter(altitudeAdapter);
        cardAltitudePicker.addScrollListener(this);

        Takeoff item = (Takeoff) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(item.getTakeoffAltitude()));
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, LengthUnit startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, LengthUnit oldValue, LengthUnit newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, LengthUnit startValue, LengthUnit endValue) {
        switch (wheel.getId()) {
            case R.id.altitudePicker:
                final double baseValue = endValue.toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    Takeoff item = (Takeoff) missionItem;
                    item.setTakeoffAltitude(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
