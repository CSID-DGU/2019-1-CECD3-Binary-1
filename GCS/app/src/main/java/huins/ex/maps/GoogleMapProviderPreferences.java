package huins.ex.maps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import huins.ex.R;


/**
 * Created by suhak on 15. 6. 23.
 */
public class GoogleMapProviderPreferences extends MapProviderPreferences {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_google_maps);
        setupPreferences();
    }

    private void setupPreferences() {
        final Context context = getActivity().getApplicationContext();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        final String mapTypeKey = "pref_map_type_key";
        final Preference mapTypePref = findPreference(mapTypeKey);
        if (mapTypePref != null) {
            mapTypePref.setSummary(sharedPref.getString(mapTypeKey, ""));
            mapTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mapTypePref.setSummary(newValue.toString());
                    return true;
                }
            });
        }
    }

    @Override
    public MapProvider getMapProvider() {
        return MapProvider.GOOGLE_MAP;
    }
}
