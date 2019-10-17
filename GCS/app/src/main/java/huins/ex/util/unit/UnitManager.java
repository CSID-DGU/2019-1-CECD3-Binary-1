package huins.ex.util.unit;

import android.content.Context;

import java.util.Locale;

import huins.ex.util.constants.GCSPreferences;
import huins.ex.util.unit.systems.ImperialUnitSystem;
import huins.ex.util.unit.systems.MetricUnitSystem;
import huins.ex.util.unit.systems.UnitSystem;

/**
 * Created by fhuya on 11/11/14.
 */
public class UnitManager {

    private static GCSPreferences GCSPrefs;
    private static MetricUnitSystem metricUnitSystem;
    private static ImperialUnitSystem imperialUnitSystem;

    public static UnitSystem getUnitSystem(Context context){
        if(GCSPrefs == null)
            GCSPrefs = new GCSPreferences(context);

        final int unitSystemType = GCSPrefs.getUnitSystemType();
        switch(unitSystemType){
            case UnitSystem.AUTO:
            default:
                Locale locale = Locale.getDefault();
                if(Locale.US.equals(locale)) {
                    if(imperialUnitSystem == null)
                        imperialUnitSystem = new ImperialUnitSystem();
                    return imperialUnitSystem;
                }
                else {
                    if (metricUnitSystem == null)
                        metricUnitSystem = new MetricUnitSystem();
                    return metricUnitSystem;
                }

            case UnitSystem.METRIC:
                if(metricUnitSystem == null)
                    metricUnitSystem = new MetricUnitSystem();
                return metricUnitSystem;

            case UnitSystem.IMPERIAL:
                if(imperialUnitSystem == null)
                    imperialUnitSystem = new ImperialUnitSystem();
                return imperialUnitSystem;
        }
    }
}
