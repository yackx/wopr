package be.sugoi.wopr;

import com.badlogic.gdx.math.Vector2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class WGS84Test {
    private final float lat;
    private final float lon;
    private final Vector2 expected;

    public WGS84Test(float lat, float lon, Vector2 expected) {
        this.lat = lat;
        this.lon = lon;
        this.expected = expected;
    }

    @Parameters
    public static List<Object[]> data() {
        return List.of(new Object[][] {
            { -90f, 0f, new Vector2(0.5f, 0f) },
            { -89f, 0f, new Vector2(0.5f, 0f) },
            { -85f, 0f, new Vector2(0.5f, 0f) },
            { -84.99f, 0f, new Vector2(0.5f, 0.001956379506736994f) },
            { -80f, 0f, new Vector2(0.5f, 0.1122593954205513f) },
            { -60f, 0f, new Vector2(0.5f, 0.2903996407985687f) },
            { -45f, 0f, new Vector2(0.5f, 0.35972505807876587f) },
            { 0f, 0f, new Vector2(0.5f, 0.5f) },
            { 45f, 0f, new Vector2(0.5f, 0.6402749419212341f) },
            { 90f, 0f, new Vector2(0.5f, 1.0f) },
            { 0f, 180f, new Vector2(1.0f, 0.5f) },
            { 0f, -180f, new Vector2(0.0f, 0.5f) }
        });
    }

    @Test
    public void testWgs84ToMercator() {
        Vector2 result = WGS84.wgs84ToMercator(new Vector2(lon, lat));
        assertEquals(expected.x, result.x, 0.000001);
        assertEquals(expected.y, result.y, 0.000001);
    }
}
