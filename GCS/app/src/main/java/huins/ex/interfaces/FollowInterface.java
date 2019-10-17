package huins.ex.interfaces;

import android.os.Bundle;

import huins.ex.proto.Flight;
import huins.ex.proto.action.Action;
import huins.ex.proto.gcs.follow.FollowType;

import static huins.ex.proto.gcs.action.FollowMeActions.ACTION_DISABLE_FOLLOW_ME;
import static huins.ex.proto.gcs.action.FollowMeActions.ACTION_ENABLE_FOLLOW_ME;
import static huins.ex.proto.gcs.action.FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS;
import static huins.ex.proto.gcs.action.FollowMeActions.EXTRA_FOLLOW_TYPE;

/**
 * Created by suhak on 15. 7. 16.
 */
public class FollowInterface {

    public static void enableFollowMe(Flight flight, FollowType followType) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_FOLLOW_TYPE, followType);
        flight.performAsyncAction(new Action(ACTION_ENABLE_FOLLOW_ME, params));
    }

    public static void updateFollowParams(Flight flight, Bundle params){
        flight.performAsyncAction(new Action(ACTION_UPDATE_FOLLOW_PARAMS, params));
    }

    /**
     * Disables follow me is enabled.
     */
    public static void disableFollowMe(Flight flight) {
        flight.performAsyncAction(new Action(ACTION_DISABLE_FOLLOW_ME));
    }
}
