package huins.ex.util;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import huins.ex.proto.Coordinate;

/**
 * Created by suhak on 15. 6. 25.
 */
public class FlightLocationHelper {
    static public LatLng CoordToLatLang(Coordinate coord) {
        return new LatLng(coord.getLatitude(), coord.getLongitude());
    }

    public static Coordinate LatLngToCoord(LatLng point) {
        return new Coordinate((float)point.latitude, (float) point.longitude);
    }

    public static Coordinate LocationToCoord(Location location) {
        return new Coordinate((float) location.getLatitude(), (float) location.getLongitude());
    }

    public static int scaleDpToPixels(double value, Resources res) {
        final float scale = res.getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }

}
