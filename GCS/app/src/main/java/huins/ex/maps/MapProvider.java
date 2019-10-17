package huins.ex.maps;

import huins.ex.proto.MapInterface;

/**
 * Created by suhak on 15. 6. 23.
 */
public enum MapProvider {

    GOOGLE_MAP {
        @Override
        public MapInterface getMapFragment() {

            return new GoogleMapFragment();
        }

        @Override
        public MapProviderPreferences getMapProviderPreferences() {
            return new GoogleMapProviderPreferences();
        }
    };


    public abstract MapInterface getMapFragment();


    public abstract MapProviderPreferences getMapProviderPreferences();

    public static MapProvider getMapProvider(String mapName) {
        if (mapName == null) {
            return null;
        }

        try {
            return MapProvider.valueOf(mapName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static final MapProvider DEFAULT_MAP_PROVIDER = GOOGLE_MAP;
}
