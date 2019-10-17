package huins.ex.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.analytics.HitBuilders;

import huins.ex.core.ErrorType;
import huins.ex.proto.Flight;
import huins.ex.proto.attribute.AttributeEvent;
import huins.ex.proto.attribute.AttributeEventExtra;
import huins.ex.util.analytics.GAUtils;

/**
 * Created by suhak on 15. 6. 26.
 */
public class NotificationHandler {
    interface NotificationProvider {
        void onTerminate();
    }

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.AUTOPILOT_ERROR);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case AttributeEvent.AUTOPILOT_ERROR:
                    final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    final ErrorType errorType = ErrorType.getErrorById(errorName);
                    if (errorType != null && ErrorType.NO_ERROR != errorType) {
                        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                .setCategory(GAUtils.Category.FAILSAFE)
                                .setAction("Autopilot error")
                                .setLabel(errorType.getLabel(context).toString());
                        GAUtils.sendEvent(eventBuilder);
                    }
                    break;
            }
        }
    };
    private final TTSNotificationProvider mTtsNotification;

    /**
     * Handles Droidplanner's status bar notification.
     */
    //private final StatusBarNotificationProvider mStatusBarNotification;

    private final EmergencyBeepNotificationProvider mBeepNotification;

    private final Context context;
    private final Flight flight;

    public NotificationHandler(Context context, Flight dpApi) {
        this.context = context;
        this.flight = dpApi;

        mTtsNotification = new TTSNotificationProvider(context, flight);
   //     mStatusBarNotification = new StatusBarNotificationProvider(context, flight);
        mBeepNotification = new EmergencyBeepNotificationProvider(context);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
    }

    /**
     * Release resources used by the notification handler. After calling this
     * method, this object should no longer be used.
     */
    public void terminate() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
        mTtsNotification.onTerminate();
   //     mStatusBarNotification.onTerminate();
        mBeepNotification.onTerminate();
    }
}
