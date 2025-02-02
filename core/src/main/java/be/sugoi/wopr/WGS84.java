package be.sugoi.wopr;

import com.badlogic.gdx.math.Vector2;

import static java.lang.Math.*;

/// WGS84 conversions
public class WGS84 {

    /// Convert latitude to Mercator Y screen coordinates
    /// @param lat
    /// Latitude in degrees.
    /// -85 or below yields 0 ; 85 or above yields 1.
    /// @return Screen Y coordinates `[0f-1f]` with `0.5` at the equator
    public static float latToMercator(float lat) {
        if (lat >= 85.0f) {
            return 1.0f; // North Pole
        }
        if (lat <= -85.0f) {
            return 0.0f; // South Pole
        }
        lat = -lat;
        var merc = 0.5f - log(tan(PI / 4 + toRadians(lat) / 2)) / (2 * PI);
        assert merc >= 0f && merc <= 1f : String.format("lat: %f -> %f", -lat, merc);
        return (float) merc;
    }

    /// Convert longitude to Mercator X coordinates
    /// @param lon Longitude in degrees
    /// @return Screen X coordinates `[0f-1f]`
    public static float lonToMercator(float lon) {
        return (lon + 180.0f) / 360.0f;
    }

    /// Convert WGS84 coordinates to normalized Mercator screen coordinates (0..1 range)
    /// @param wgs84 Coord (lon, lat)
    /// @return Normalized Mercator coordinates
    public static Vector2 wgs84ToMercator(Vector2 wgs84) {
        final float x = lonToMercator(wgs84.x);
        final float y = latToMercator(wgs84.y);
        return new Vector2(x, y);
    }

    public static void main(String[] args) {
        float lat = 45.0f;
        float lon = 90.0f;
        Vector2 mercator = wgs84ToMercator(new Vector2(lon, lat));
        System.out.println("Mercator X: " + mercator.x + ", Y: " + mercator.y);
    }
}
