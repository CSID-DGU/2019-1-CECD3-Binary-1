package huins.ex.view.fragments.action;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import huins.ex.R;
import huins.ex.model.MissionProxy;
import huins.ex.proto.Flight;
import huins.ex.proto.RadioButtonCenter;
import huins.ex.view.dialogs.YesNoDialog;
import huins.ex.view.dialogs.YesNoWithPrefsDialog;
import huins.ex.view.fragments.helper.ApiListenerFragment;

/**
 * Created by suhak on 15. 7. 29.
 */
public class MissionActionBarFragment extends ApiListenerFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_actionbar, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RadioButtonCenter _mission_save = (RadioButtonCenter) view.findViewById(R.id.mission_save);
        final RadioButtonCenter _mission_load = (RadioButtonCenter) view.findViewById(R.id.mission_load);

        _mission_save.setOnClickListener(this);
        _mission_load.setOnClickListener(this);


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement actioninterfacelistener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        final Flight flight = getFlight();

        switch (view.getId()) {
            case R.id.mission_save:
                final MissionProxy missionProxy = getMissionProxy();
                if (missionProxy.getItems().isEmpty() || missionProxy.hasTakeoffAndLandOrRTL()) {
                    missionProxy.sendMissionToAPM(flight);
                } else {
                    YesNoWithPrefsDialog dialog = YesNoWithPrefsDialog.newInstance(
                            getContext(), "Mission Upload",
                            "Do you want to append a Takeoff and RTL to your " + "mission?", "Ok",
                            "Skip", new YesNoDialog.Listener() {

                                @Override
                                public void onYes() {
                                    missionProxy.addTakeOffAndRTL();
                                    missionProxy.sendMissionToAPM(flight);
                                }

                                @Override
                                public void onNo() {
                                    missionProxy.sendMissionToAPM(flight);
                                }
                            }, getString(R.string.pref_auto_insert_mission_takeoff_rtl_land_key));

                    if (dialog != null) {
                        dialog.show(getActivity().getSupportFragmentManager(), "Mission Upload check.");
                    }
                }
                break;
            case R.id.mission_load:
                flight.loadWaypoints();
                break;
        }
    }

    @Override
    public void onApiConnected() {

    }

    @Override
    public void onApiDisconnected() {

    }
}
