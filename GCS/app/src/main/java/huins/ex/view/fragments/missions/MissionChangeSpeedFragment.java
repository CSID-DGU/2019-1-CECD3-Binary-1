package huins.ex.view.fragments.missions;

import android.view.View;


import org.beyene.sius.unit.composition.speed.SpeedUnit;

import huins.ex.R;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.command.ChangeSpeed;
import huins.ex.util.unit.providers.speed.SpeedUnitProvider;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.SpeedWheelAdapter;

public class MissionChangeSpeedFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<SpeedUnit> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_change_speed;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CHANGE_SPEED));

        final SpeedUnitProvider speedUnitProvider = getSpeedUnitProvider();
        final SpeedWheelAdapter adapter = new SpeedWheelAdapter(getContext(), R.layout.wheel_text_centered,
                speedUnitProvider.boxBaseValueToTarget(1), speedUnitProvider.boxBaseValueToTarget(20));
        CardWheelHorizontalView<SpeedUnit> cardAltitudePicker = (CardWheelHorizontalView<SpeedUnit>) view.findViewById
                (R.id.picker1);
        cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addScrollListener(this);

        ChangeSpeed item = (ChangeSpeed) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue(speedUnitProvider.boxBaseValueToTarget(item.getSpeed()));
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, SpeedUnit startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, SpeedUnit oldValue, SpeedUnit newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, SpeedUnit startValue, SpeedUnit endValue) {
        switch (wheel.getId()) {
            case R.id.picker1:
                double baseValue = endValue.toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    ChangeSpeed item = (ChangeSpeed) missionItem;
                    item.setSpeed(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
