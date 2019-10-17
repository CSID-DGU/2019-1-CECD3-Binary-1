package huins.ex.maps;

import android.preference.PreferenceFragment;

/**
 * Created by suhak on 15. 6. 23.
 */
public abstract  class MapProviderPreferences extends PreferenceFragment {

    public abstract MapProvider getMapProvider();
}
