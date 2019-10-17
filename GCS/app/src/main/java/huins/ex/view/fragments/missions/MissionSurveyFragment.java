package huins.ex.view.fragments.missions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import org.beyene.sius.unit.length.LengthUnit;

import java.util.Collections;
import java.util.List;

import huins.ex.R;
import huins.ex.model.MissionProxy;
import huins.ex.model.adapters.CamerasAdapter;
import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeType;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.complex.CameraDetail;
import huins.ex.proto.mission.item.complex.Survey;
import huins.ex.proto.mission.item.complex.SurveyDetail;
import huins.ex.proto.property.CameraProxy;
import huins.ex.util.unit.providers.area.AreaUnitProvider;
import huins.ex.util.unit.providers.length.LengthUnitProvider;
import huins.ex.view.widgets.spinnerWheel.CardWheelHorizontalView;
import huins.ex.view.widgets.spinnerWheel.adapters.LengthWheelAdapter;
import huins.ex.view.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import huins.ex.view.widgets.spinners.SpinnerSelfSelect;

public class MissionSurveyFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, SpinnerSelfSelect.OnSpinnerItemSelectedListener,
        Flight.OnMissionItemsBuiltCallback {

    private static final String TAG = MissionSurveyFragment.class.getSimpleName();

    private static final IntentFilter eventFilter = new IntentFilter(MissionProxy.ACTION_MISSION_PROXY_UPDATE);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MissionProxy.ACTION_MISSION_PROXY_UPDATE.equals(action)) {
                updateViews();
            }
        }
    };

    private CardWheelHorizontalView<Integer> mOverlapPicker;
    private CardWheelHorizontalView<Integer> mAnglePicker;
    private CardWheelHorizontalView<LengthUnit> mAltitudePicker;
    private CardWheelHorizontalView<Integer> mSidelapPicker;

    public TextView distanceBetweenLinesTextView;
    public TextView areaTextView;
    public TextView distanceTextView;
    public TextView footprintTextView;
    public TextView groundResolutionTextView;
    public TextView numberOfPicturesView;
    public TextView numberOfStripsView;
    public TextView lengthView;
    private CamerasAdapter cameraAdapter;
    private SpinnerSelfSelect cameraSpinner;

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_survey;
    }

    @Override
    protected List<Survey> getMissionItems() {
        return (List<Survey>) super.getMissionItems();
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getContext();

        CameraProxy camera = getFlight().getAttribute(AttributeType.CAMERA);
        List<CameraDetail> cameraDetails = camera == null
                ? Collections.<CameraDetail>emptyList()
                : camera.getAvailableCameraInfos();
        cameraAdapter = new CamerasAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, cameraDetails);

        cameraSpinner = (SpinnerSelfSelect) view.findViewById(R.id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnSpinnerItemSelectedListener(this);

        mAnglePicker = (CardWheelHorizontalView) view.findViewById(R.id.anglePicker);
        mAnglePicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 180, "%dº"));

        mOverlapPicker = (CardWheelHorizontalView) view.findViewById(R.id.overlapPicker);
        mOverlapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 99, "%d %%"));

        mSidelapPicker = (CardWheelHorizontalView) view.findViewById(R.id.sidelapPicker);
        mSidelapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 99, "%d %%"));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        mAltitudePicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudePicker);
        mAltitudePicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(0), lengthUP.boxBaseValueToTarget(200)));

        areaTextView = (TextView) view.findViewById(R.id.areaTextView);
        distanceBetweenLinesTextView = (TextView) view.findViewById(R.id.distanceBetweenLinesTextView);
        footprintTextView = (TextView) view.findViewById(R.id.footprintTextView);
        groundResolutionTextView = (TextView) view.findViewById(R.id.groundResolutionTextView);
        distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        numberOfPicturesView = (TextView) view.findViewById(R.id.numberOfPicturesTextView);
        numberOfStripsView = (TextView) view.findViewById(R.id.numberOfStripsTextView);
        lengthView = (TextView) view.findViewById(R.id.lengthTextView);

        updateViews();
        updateCamera();

        mAnglePicker.addScrollListener(this);
        mOverlapPicker.addScrollListener(this);
        mSidelapPicker.addScrollListener(this);
        mAltitudePicker.addScrollListener(this);

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY));

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onSpinnerItemSelected(Spinner spinner, int position) {
        if (spinner.getId() == R.id.cameraFileSpinner) {
            CameraDetail cameraInfo = cameraAdapter.getItem(position);
            for (Survey survey : getMissionItems()) {
                survey.getSurveyDetail().setCameraDetail(cameraInfo);
            }

            onScrollingEnded(mAnglePicker, 0, 0);
        }
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
            case R.id.anglePicker:
            case R.id.altitudePicker:
            case R.id.overlapPicker:
            case R.id.sidelapPicker:
                final Flight flight = getFlight();
                try {
                    final List<Survey> surveyList = getMissionItems();
                    if (!surveyList.isEmpty()) {
                        for (final Survey survey : surveyList) {
                            SurveyDetail surveyDetail = survey.getSurveyDetail();
                            surveyDetail.setAltitude(mAltitudePicker.getCurrentValue().toBase().getValue());
                            surveyDetail.setAngle(mAnglePicker.getCurrentValue());
                            surveyDetail.setOverlap(mOverlapPicker.getCurrentValue());
                            surveyDetail.setSidelap(mSidelapPicker.getCurrentValue());
                        }

                        final MissionItem.ComplexItem<Survey>[] surveys = surveyList
                                .toArray(new MissionItem.ComplexItem[surveyList.size()]);

                        flight.buildMissionItemsAsync(this, surveys);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while building the survey.", e);
                }
                break;
        }
    }

    private void checkIfValid(Survey survey) {
        if (mAltitudePicker == null)
            return;

        if (survey.isValid())
            mAltitudePicker.setBackgroundResource(R.drawable.bg_cell_white);
        else
            mAltitudePicker.setBackgroundColor(Color.RED);
    }

    private void updateViews() {
        if (getActivity() == null)
            return;

        updateTextViews();
        updateSeekBars();
    }

    private void updateCamera() {
        List<Survey> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            Survey survey = surveyList.get(0);
            final int cameraSelection = cameraAdapter.getPosition(survey.getSurveyDetail().getCameraDetail());
            cameraSpinner.setSelection(Math.max(cameraSelection, 0));
        }
    }

    private void updateSeekBars() {
        List<Survey> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            Survey survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            if (surveyDetail != null) {
                mAnglePicker.setCurrentValue((int) surveyDetail.getAngle());
                mOverlapPicker.setCurrentValue((int) surveyDetail.getOverlap());
                mSidelapPicker.setCurrentValue((int) surveyDetail.getSidelap());
                mAltitudePicker.setCurrentValue(getLengthUnitProvider().boxBaseValueToTarget(surveyDetail.getAltitude()));
            }

            checkIfValid(survey);
        }
    }

    private void updateTextViews() {
        boolean setDefault = true;
        List<Survey> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            Survey survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            try {
                final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
                final AreaUnitProvider areaUnitProvider = getAreaUnitProvider();

                footprintTextView.setText(String.format("%s: %s x %s",
                        getString(R.string.footprint),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLateralFootPrint()),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLongitudinalFootPrint())));

                groundResolutionTextView.setText(String.format("%s: %s /px",
                        getString(R.string.ground_resolution),
                        areaUnitProvider.boxBaseValueToTarget(surveyDetail.getGroundResolution())));

                distanceTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_pictures),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLongitudinalPictureDistance())));

                distanceBetweenLinesTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_lines),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLateralPictureDistance())));

                areaTextView.setText(String.format("%s: %s", getString(R.string.area),
                        areaUnitProvider.boxBaseValueToTarget(survey.getPolygonArea())));

                lengthView.setText(String.format("%s: %s", getString(R.string.mission_length),
                        lengthUnitProvider.boxBaseValueToTarget(survey.getGridLength())));

                numberOfPicturesView.setText(String.format("%s: %d", getString(R.string.pictures),
                        survey.getCameraCount()));

                numberOfStripsView.setText(String.format("%s: %d", getString(R.string.number_of_strips),
                        survey.getNumberOfLines()));

                setDefault = false;
            } catch (Exception e) {
                setDefault = true;
            }
        }

        if (setDefault) {
            footprintTextView.setText(getString(R.string.footprint) + ": ???");
            groundResolutionTextView.setText(getString(R.string.ground_resolution) + ": ???");
            distanceTextView.setText(getString(R.string.distance_between_pictures) + ": ???");
            distanceBetweenLinesTextView.setText(getString(R.string.distance_between_lines)
                    + ": ???");
            areaTextView.setText(getString(R.string.area) + ": ???");
            lengthView.setText(getString(R.string.mission_length) + ": ???");
            numberOfPicturesView.setText(getString(R.string.pictures) + "???");
            numberOfStripsView.setText(getString(R.string.number_of_strips) + "???");
        }
    }

    @Override
    public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
        for (MissionItem.ComplexItem<Survey> item : complexItems) {
            checkIfValid((Survey) item);
        }

        getMissionProxy().notifyMissionUpdate();
    }
}
