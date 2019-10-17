package huins.ex.model;

import android.os.Bundle;

import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;
import huins.ex.proto.mission.Mission;
import huins.ex.proto.mission.MissionItemType;
import huins.ex.proto.mission.action.MissionActions;
import huins.ex.proto.mission.item.MissionItem;
import huins.ex.proto.mission.item.MissionItem.ComplexItem;

/**
 * Created by suhak on 15. 7. 7.
 */
public class MissionApi {
    public MissionApi() {
    }

    public static void generateFlight(Flight flight) {
        flight.performAsyncAction(new Action(MissionActions.ACTION_GENERATE_DRONIE));
    }

    public static void setMission(Flight flight, Mission mission, boolean pushToDrone) {
        Bundle params = new Bundle();
        params.putParcelable("extra_mission", mission);
        params.putBoolean("extra_push_to_flight", pushToDrone);
        flight.performAsyncAction(new Action(MissionActions.ACTION_SET_MISSION, params));
    }

    public static void loadWaypoints(Flight flight) {
        flight.performAsyncAction(new Action(MissionActions.ACTION_LOAD_WAYPOINTS));
    }

    private static Action buildComplexMissionItem(Flight flight, Bundle itemBundle) {
        Action payload = new Action(MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM, itemBundle);
        boolean result = flight.performAction(payload);
        return result?payload:null;
    }

    public static <T extends MissionItem> T buildMissionItem(Flight flight, ComplexItem<T> complexItem) {
        MissionItem missionItem = (MissionItem)complexItem;
        Bundle payload = missionItem.getType().storeMissionItem(missionItem);
        if(payload == null) {
            return null;
        } else {
            Action result = buildComplexMissionItem(flight, payload);
            if(result != null) {
                MissionItem updatedItem = MissionItemType.restoreMissionItemFromBundle(result.getData());
                complexItem.copy((T)updatedItem); //suhak
                return (T)complexItem;
            } else {
                return null;
            }
        }
    }
}

