package huins.ex.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

import huins.ex.maps.MapProvider;
import huins.ex.util.constants.GCSPreferences;

/**
 * Created by suhak on 15. 6. 23.
 */
public class UIUtils {

    public static MapProvider getMapProvider(Context context) {
        GCSPreferences prefs = new GCSPreferences(context);
        final String mapProviderName = prefs.getMapProviderName();

        return mapProviderName == null ? MapProvider.DEFAULT_MAP_PROVIDER : MapProvider.getMapProvider(mapProviderName);
    }

    public static void updateUILanguage(Context context) {
        GCSPreferences prefs = new GCSPreferences(context);
        if (prefs.isEnglishDefaultLanguage()) {
            Configuration config = new Configuration();
            config.locale = Locale.ENGLISH;

            final Resources res = context.getResources();
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
    }
}
