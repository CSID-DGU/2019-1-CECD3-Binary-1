package huins.ex.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.content.LocalBroadcastManager;

import huins.ex.R;
import huins.ex.proto.Flight;
import huins.ex.util.constants.GCSPreferences;

public class EmergencyBeepNotificationProvider implements NotificationHandler.NotificationProvider {

    private static final IntentFilter eventFilter = new IntentFilter(
            Flight.ACTION_GROUND_COLLISION_IMMINENT);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Flight.ACTION_GROUND_COLLISION_IMMINENT.equals(action)) {
                if (appPrefs.getImminentGroundCollisionWarning() && intent.getBooleanExtra(Flight
                        .EXTRA_IS_GROUND_COLLISION_IMMINENT, false)) {
                    mPool.play(beepBeep, 1f, 1f, 1, 1, 1f);
                } else {
                    mPool.stop(beepBeep);
                }
            }
        }
    };

    private SoundPool mPool;
    private int beepBeep;

    private final Context context;
    private final GCSPreferences appPrefs;

    public EmergencyBeepNotificationProvider(Context context) {
        this.context = context;
        appPrefs = new GCSPreferences(context);
        mPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        beepBeep = mPool.load(context, R.raw.beep_beep, 1);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onTerminate() {
        mPool.release();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
    }

}
