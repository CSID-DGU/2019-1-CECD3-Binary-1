package huins.ex.util.Math;

import java.util.ArrayList;
import java.util.List;

import huins.ex.core.helpers.coordinates.Coord2D;
import huins.ex.core.helpers.coordinates.Coord3D;
import huins.ex.proto.Coordinate;
import huins.ex.proto.CoordinateExtend;

/**
 * Created by suhak on 15. 6. 23.
 */
public class MathUtils {

    private static final double RADIUS_OF_EARTH = 6378137.0D;
    public static final int SIGNAL_MAX_FADE_MARGIN = 50;
    public static final int SIGNAL_MIN_FADE_MARGIN = 6;

    public MathUtils() {
    }

    public static double getDistance3D(CoordinateExtend from, CoordinateExtend to){
        if(from == null || to == null)
            return -1;

        final double distance2d = getDistance2D(from, to);
        double distanceSqr = Math.pow(distance2d, 2);
        double altitudeSqr = Math.pow(to.getAltitude() - from.getAltitude(), 2);

        return Math.sqrt(altitudeSqr + distanceSqr);
    }

    public static double getDistance2D(Coordinate from, Coordinate to){
        if(from == null || to == null)
            return -1;

        return RADIUS_OF_EARTH * Math.toRadians(getArcInRadians(from, to));
    }


    public static double getDistance(Coordinate from, Coordinate to) {
        return 6378137.0D * Math.toRadians(getArcInRadians(from, to));
    }

    static double getArcInRadians(Coordinate from, Coordinate to) {
        double latitudeArc = Math.toRadians(from.getLatitude() - to.getLatitude());
        double longitudeArc = Math.toRadians(from.getLongitude() - to.getLongitude());
        double latitudeH = Math.sin(latitudeArc * 0.5D);
        latitudeH *= latitudeH;
        double lontitudeH = Math.sin(longitudeArc * 0.5D);
        lontitudeH *= lontitudeH;
        double tmp = Math.cos(Math.toRadians(from.getLatitude())) * Math.cos(Math.toRadians(to.getLatitude()));
        return Math.toDegrees(2.0D * Math.asin(Math.sqrt(latitudeH + tmp * lontitudeH)));
    }

    public static List<Coordinate> simplify(List<Coordinate> list, double tolerance) {
        int index = 0;
        double dmax = 0.0D;
        int lastIndex = list.size() - 1;

        for(int ResultList = 1; ResultList < lastIndex; ++ResultList) {
            double recResults1 = pointToLineDistance((Coordinate)list.get(0), (Coordinate)list.get(lastIndex), (Coordinate)list.get(ResultList));
            if(recResults1 > dmax) {
                index = ResultList;
                dmax = recResults1;
            }
        }

        ArrayList var10 = new ArrayList();
        if(dmax > tolerance) {
            List var11 = simplify(list.subList(0, index + 1), tolerance);
            List recResults2 = simplify(list.subList(index, lastIndex + 1), tolerance);
            var11.remove(var11.size() - 1);
            var10.addAll(var11);
            var10.addAll(recResults2);
        } else {
            var10.add(list.get(0));
            var10.add(list.get(lastIndex));
        }

        return var10;
    }

    public static double pointToLineDistance(Coordinate L1, Coordinate L2, Coordinate P) {
        double A = P.getLatitude() - L1.getLatitude();
        double B = P.getLongitude() - L1.getLongitude();
        double C = L2.getLatitude() - L1.getLatitude();
        double D = L2.getLongitude() - L1.getLongitude();
        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = dot / len_sq;
        double xx;
        double yy;
        if(param < 0.0D) {
            xx = L1.getLatitude();
            yy = L1.getLongitude();
        } else if(param > 1.0D) {
            xx = L2.getLatitude();
            yy = L2.getLongitude();
        } else {
            xx = L1.getLatitude() + param * C;
            yy = L1.getLongitude() + param * D;
        }

        return Math.hypot(xx - P.getLatitude(), yy - P.getLongitude());
    }
    public static class SplinePath {

        /**
         * Used as tag for logging.
         */
        private static final String TAG = SplinePath.class.getSimpleName();

        private final static int SPLINE_DECIMATION = 20;

        /**
         * Process the given map coordinates, and return a set of coordinates
         * describing the spline path.
         *
         * @param points
         *            map coordinates decimation factor
         * @return set of coordinates describing the spline path
         */
        public static List<Coordinate> process(List<Coordinate> points) {
            final int pointsCount = points.size();
            if (pointsCount < 4) {
                System.err.println("Not enough points!");
                return points;
            }

            final List<Coordinate> results = processPath(points);
            results.add(0, points.get(0));
            results.add(points.get(pointsCount - 1));
            return results;
        }

        private static List<Coordinate> processPath(List<Coordinate> points) {
            final List<Coordinate> results = new ArrayList<Coordinate>();
            for (int i = 3; i < points.size(); i++) {
                results.addAll(processPathSegment(points.get(i - 3), points.get(i - 2),
                        points.get(i - 1), points.get(i)));
            }
            return results;
        }

        private static List<Coordinate> processPathSegment(Coordinate l1, Coordinate l2, Coordinate l3, Coordinate l4) {
            Spline spline = new Spline(l1, l2, l3, l4);
            return spline.generateCoordinates(SPLINE_DECIMATION);
        }

    }

    public static class Spline {

        private static final float SPLINE_TENSION = 1.6f;

        private Coordinate p0;
        private Coordinate p0_prime;
        private Coordinate a;
        private Coordinate b;

        public Spline(Coordinate pMinus1, Coordinate p0, Coordinate p1, Coordinate p2) {
            this.p0 = p0;

            // derivative at a point is based on difference of previous and next
            // points
            p0_prime = p1.subtract(pMinus1).dot(1 / SPLINE_TENSION);
            Coordinate p1_prime = p2.subtract(this.p0).dot(1 / SPLINE_TENSION);

            // compute a and b coords used in spline formula
            a = Coordinate.sum(this.p0.dot(2), p1.dot(-2), p0_prime, p1_prime);
            b = Coordinate.sum(this.p0.dot(-3), p1.dot(3), p0_prime.dot(-2), p1_prime.negate());
        }

        public List<Coordinate> generateCoordinates(int decimation) {
            ArrayList<Coordinate> result = new ArrayList<Coordinate>();
            float step = 1f / decimation;
            for (float i = 0; i < 1; i += step) {
                result.add(evaluate(i));
            }

            return result;
        }

        private Coordinate evaluate(float t) {
            float tSquared = t * t;
            float tCubed = tSquared * t;

            return Coordinate.sum(a.dot(tCubed), b.dot(tSquared), p0_prime.dot(t), p0);
        }

    }

    /**
     * Computes the heading between two coordinates
     *
     * @return heading in degrees
     */
    public static double getHeadingFromCoordinates(Coordinate fromLoc, Coordinate toLoc) {
        double fLat = Math.toRadians(fromLoc.getLatitude());
        double fLng = Math.toRadians(fromLoc.getLongitude());
        double tLat = Math.toRadians(toLoc.getLatitude());
        double tLng = Math.toRadians(toLoc.getLongitude());

        double degree = Math.toDegrees(Math.atan2(
                Math.sin(tLng - fLng) * Math.cos(tLat),
                Math.cos(fLat) * Math.sin(tLat) - Math.sin(fLat) * Math.cos(tLat)
                        * Math.cos(tLng - fLng)));

        if (degree >= 0) {
            return degree;
        } else {
            return 360 + degree;
        }
    }

    /**
     * Extrapolate latitude/longitude given a heading and distance thanks to
     * http://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param origin
     *            Point of origin
     * @param bearing
     *            bearing to navigate
     * @param distance
     *            distance to be added
     * @return New point with the added distance
     */
    public static Coordinate newCoordFromBearingAndDistance(Coordinate origin, double bearing,
                                                         double distance) {

        double lat = origin.getLatitude();
        double lon = origin.getLongitude();
        double lat1 = Math.toRadians(lat);
        double lon1 = Math.toRadians(lon);
        double brng = Math.toRadians(bearing);
        double dr = distance / RADIUS_OF_EARTH;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1) * Math.sin(dr)
                * Math.cos(brng));
        double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1),
                Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));

        return (new Coordinate(Math.toDegrees(lat2), Math.toDegrees(lon2)));
    }

    /**
     * Total length of the polyline in meters
     *
     * @param gridPoints
     * @return
     */
    public static double getPolylineLength(List<Coordinate> gridPoints) {
        double lenght = 0;
        for (int i = 1; i < gridPoints.size(); i++) {
            final Coordinate to = gridPoints.get(i - 1);
            if (to == null) {
                continue;
            }

            lenght += getDistance2D(gridPoints.get(i), to);
        }
        return lenght;
    }

    public static Coord2D latLongToCoord2D(Coordinate latLong) {
        return new Coord2D(latLong.getLatitude(), latLong.getLongitude());
    }

    public static Coordinate coord2DToLatLong(Coord2D coord) {
        return new Coordinate(coord.getLat(), coord.getLng());
    }

    public static CoordinateExtend coord3DToLatLongAlt(Coord3D coord) {
        return new CoordinateExtend(coord.getLat(), coord.getLng(),
                coord.getAltitude());
    }

    public static Coord3D latLongAltToCoord3D(CoordinateExtend position) {
        return new Coord3D(position.getLatitude(), position.getLongitude(), position.getAltitude());
    }

    public static List<Coordinate> coord2DToLatLong(List<Coord2D> coords) {
        final List<Coordinate> points = new ArrayList<Coordinate>();
        if (coords != null && !coords.isEmpty()) {
            for (Coord2D coord : coords) {
                points.add(coord2DToLatLong(coord));
            }
        }

        return points;
    }

    public static List<Coord2D> latLongToCoord2D(List<Coordinate> points) {
        final List<Coord2D> coords = new ArrayList<Coord2D>();
        if (points != null && !points.isEmpty()) {
            for (Coordinate point : points) {
                coords.add(latLongToCoord2D(point));
            }
        }

        return coords;
    }

    public static int getSignalStrength(double fadeMargin, double remFadeMargin) {
        return (int) (MathUtils.Normalize(Math.min(fadeMargin, remFadeMargin),
                SIGNAL_MIN_FADE_MARGIN, SIGNAL_MAX_FADE_MARGIN) * 100);
    }

    private static double Constrain(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    public static double Normalize(double value, double min, double max) {
        value = Constrain(value, min, max);
        return (value - min) / (max - min);

    }
}
