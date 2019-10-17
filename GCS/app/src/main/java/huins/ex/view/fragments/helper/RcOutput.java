package huins.ex.view.fragments.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import huins.ex.core.MAVLink.MavLinkRC;
import huins.ex.core.model.Flight;

/**
 * Created by suhak on 15. 10. 12.
 */
public class RcOutput {

    private static final int DISABLE_OVERRIDE = 0;
    private static final int RC_TRIM = 1504;
    private static final int RC_RANGE = 512;
    private Context parrentContext;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Flight flight;
    public int[] rcOutputs = new int[8];

    public static final int AILERON = 0;
    public static final int ELEVATOR = 1;
    public static final int TROTTLE = 2;
    public static final int RUDDER = 3;

    public static final int RC5 = 4;
    public static final int RC6 = 5;
    public static final int RC7 = 6;
    public static final int RC8 = 7;

    public RcOutput(Flight flight, Context context) {
        this.flight = flight;
        parrentContext = context;
    }

    public void disableRcOverride() {
        if (isRcOverrided()) {
            scheduleTaskExecutor.shutdownNow();
            scheduleTaskExecutor = null;
        }
        Arrays.fill(rcOutputs, DISABLE_OVERRIDE); // Start with all channels
        // disabled, external
        // callers can enable them
        // as desired
        MavLinkRC.sendRcOverrideMsg(flight, rcOutputs); // Just to be sure send 3
        // disable
        MavLinkRC.sendRcOverrideMsg(flight, rcOutputs);
        MavLinkRC.sendRcOverrideMsg(flight, rcOutputs);
    }

    public void enableRcOverride() {
        if (!isRcOverrided()) {
            Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
            MavLinkRC.sendRcOverrideMsg(flight, rcOutputs); // Just to be sure
            // send 3
            MavLinkRC.sendRcOverrideMsg(flight, rcOutputs);
            MavLinkRC.sendRcOverrideMsg(flight, rcOutputs);
            Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
            scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    MavLinkRC.sendRcOverrideMsg(flight, rcOutputs);
         //           GCSLogger.WriteLog(GCSLogger.GCS_LOGGER.LOG_INFO, "RCOUTPUT", "data : " + String.valueOf(rcOutputs[0]) + " " + String.valueOf(rcOutputs[1]) + " " + String.valueOf(rcOutputs[2]) + " " + String.valueOf(rcOutputs[3]));
                }
            }, 0, getRcOverrideDelayMs(), TimeUnit.MILLISECONDS);
        }
    }

    private int getRcOverrideDelayMs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parrentContext);
        int rate = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_RC_override", "0"));
        if ((rate > 1) & (rate < 500)) {
            return 1000 / rate;
        } else {
            return 10;
        }
    }

    public boolean isRcOverrided() {
        return (scheduleTaskExecutor != null);
    }

    public void setRcChannel(int ch, double value) {
        if (value > +1)
            value = +1;
        if (value < -1)
            value = -1;
        rcOutputs[ch] = (int) (value * RC_RANGE + RC_TRIM);
    }
}
