package huins.ex.view.fragments.missions;

import android.view.View;

import org.beyene.sius.unit.length.LengthUnit;

import huins.ex.R;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.command.CameraTrigger;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;

public class MissionCameraTriggerFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_camera_trigger;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CAMERA_TRIGGER));

        CameraTrigger item = (CameraTrigger) getMissionItems().get(0);

        final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
        final LengthWheelAdapter adapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUnitProvider.boxBaseValueToTarget(0), lengthUnitProvider.boxBaseValueToTarget(100));
        final CardWheelHorizontalView<LengthUnit> cardAltitudePicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.picker1);
        cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addScrollListener(this);
        cardAltitudePicker.setCurrentValue(lengthUnitProvider.boxBaseValueToTarget(item.getTriggerDistance()));
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
            case R.id.picker1:
                double baseValue = endValue.toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    CameraTrigger item = (CameraTrigger) missionItem;
                    item.setTriggerDistance(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
