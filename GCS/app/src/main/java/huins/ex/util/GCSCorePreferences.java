package huins.ex.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import huins.ex.R;
import huins.ex.core.Flight.profiles.VehicleProfile;
import huins.ex.core.Flight.variables.StreamRates.Rates;
import huins.ex.core.firmware.FirmwareType;
import huins.ex.util.File.IO.VehicleProfileReader;

/**
 * Created by suhak on 15. 7. 15.
 */
public class GCSCorePreferences implements huins.ex.core.Flight.Preferences {

    /*
     * Default preference value
     */
    public static final boolean DEFAULT_USAGE_STATISTICS = true;

    // Public for legacy usage
    public SharedPreferences prefs;
    private Context context;

    public GCSCorePreferences(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Return a unique ID for the vehicle controlled by this tablet. FIXME,
     * someday let the users select multiple vehicles
     */
    public String getVehicleId() {
        String r = prefs.getString("vehicle_id", "").trim();

        // No ID yet - pick one
        if (r.isEmpty()) {
            r = UUID.randomUUID().toString();

            prefs.edit().putString("vehicle_id", r).apply();
        }
        return r;
    }

    @Override
    public FirmwareType getVehicleType() {
        String str = prefs.getString("pref_vehicle_type", FirmwareType.ARDU_COPTER.toString());
        return FirmwareType.firmwareFromString(str);
    }

    @Override
    public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
        return VehicleProfileReader.load(context, firmwareType);
    }

    @Override
    public Rates getRates() {
        final int defaultRate = 2;

        Rates rates = new Rates();

        rates.extendedStatus = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_ext_stat", "2"));
        rates.extra1 = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1", "2"));
        rates.extra2 = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2", "2"));
        rates.extra3 = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3", "2"));
        rates.position = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_position", "2"));
        rates.rcChannels = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_rc_channels",
        //"2"));
        rates.rawSensors = defaultRate; //Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_sensors",
        //"2"));
        rates.rawController = defaultRate; //Integer.parseInt(prefs.getString
        // ("pref_mavlink_stream_rate_raw_controller", "2"));

        return rates;
    }

    /**
     * @return true if google analytics reporting is enabled.
     */
    public boolean isUsageStatisticsEnabled() {
        return prefs.getBoolean(context.getString(R.string.pref_usage_statistics_key),
                DEFAULT_USAGE_STATISTICS);
    }

}
