package be.sugoi.wopr;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/// Nuke trajectory in WSG84 referential
public class Trajectory {

    /// Generate equidistant points between 2 existing WGS84 points.
    ///
    /// @param a         One point
    /// @param b         Another point
    /// @param numPoints How many in-between points
    /// @return A list of points.
    public static List<Vector2> generateEquidistantPoints(Vector2 a, Vector2 b, int numPoints) {
        List<Vector2> points = new ArrayList<>();
        double lat1 = toRadians(a.y);
        double lon1 = toRadians(a.x);
        double lat2 = toRadians(b.y);
        double lon2 = toRadians(b.x);

        for (int i = 1; i <= numPoints; i++) {
            double f = (double) i / (numPoints + 1);
            double A = sin((1 - f) * haversine(lon1, lat1, lon2, lat2)) / sin(haversine(lon1, lat1, lon2, lat2));
            double B = sin(f * haversine(lon1, lat1, lon2, lat2)) / sin(haversine(lon1, lat1, lon2, lat2));
            double x = A * cos(lat1) * cos(lon1) + B * cos(lat2) * cos(lon2);
            double y = A * cos(lat1) * sin(lon1) + B * cos(lat2) * sin(lon2);
            double z = A * sin(lat1) + B * sin(lat2);
            double lat = atan2(z, sqrt(x * x + y * y));
            double lon = atan2(y, x);
            points.add(new Vector2((float) toDegrees(lon), (float) toDegrees(lat)));
        }
        return points;
    }

    /// Calculate the distance between 2 WGS84 points using haversine
    ///
    /// @param lon1 Point 1 longitude (rad)
    /// @param lat1 Point 1 latitude (rad)
    /// @param lon2 Point 2 longitude (rad)
    /// @param lat2 Point 2 latitude (rad)
    /// @return Distance
    public static double haversine(double lon1, double lat1, double lon2, double lat2) {
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2);
        return 2 * atan2(sqrt(a), sqrt(1 - a));
    }

    /// Calculate the distance between 2 WGS84 points using haversine
    ///
    /// @param v1 Point 1 (degrees)
    /// @param v2 Point 2 (degrees)
    /// @return Distance
    public static double haversine(Vector2 v1, Vector2 v2) {
        return haversine(toRadians(v1.x), toRadians(v1.y), toRadians(v2.x), toRadians(v2.y));
    }

    public static void main(String[] args) {
        Vector2 a = new Vector2(3.7550468341441388f, 50.34633372142417f);
        Vector2 b = new Vector2(5.215422810614257f, 51.25873280757304f);
        List<Vector2> points = generateEquidistantPoints(a, b, 10);
        points.forEach(point -> System.out.println("Lat: " + point.y + ", Lon: " + point.x));
        var distance = haversine(
            toRadians(a.x), toRadians(a.y), toRadians(b.x), toRadians(b.y)
        );
        System.out.println("Distance: " + distance);
    }
}
