package huins.ex.view.fragments.missions;

import android.view.View;

import org.beyene.sius.unit.length.LengthUnit;

import huins.ex.R;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.spatial.RegionOfInterest;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.CardTextInputView;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;

public class MissionRegionOfInterestFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit>, CardTextInputView.OnTextChangeListener {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_roi;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.REGION_OF_INTEREST));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> altitudePicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addScrollListener(this);

        final RegionOfInterest item = (RegionOfInterest) getMissionItems().get(0);

        altitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget((item).getCoordinate().getAltitude()));

        CardTextInputView laditudePicker = (CardTextInputView) view.findViewById(R.id.waypointLatitudeEditor);
        CardTextInputView longitudePicker = (CardTextInputView) view.findViewById(R.id.waypointLongitudEditor);

        laditudePicker.addEditListener(this);
        longitudePicker.addEditListener(this);

        laditudePicker.setCurrentValue(item.getCoordinate().getLatitude());
        longitudePicker.setCurrentValue(item.getCoordinate().getLongitude());
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
                    ((RegionOfInterest) missionItem).getCoordinate().setAltitude(baseValue);
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
                    ((RegionOfInterest) item).getCoordinate().setLatitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
            case R.id.waypointLongitudEditor:
                for (MissionItem item : getMissionItems()) {
                    ((RegionOfInterest) item).getCoordinate().setLongitude(value);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
