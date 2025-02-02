package be.sugoi.wopr;

import be.sugoi.wopr.programs.thermo.entities.Polygon;
import com.badlogic.gdx.math.Vector2;

public class Projection {
    private static final Vector2 WORLD_CENTER = new Vector2(Main.WORLD_WIDTH/2, Main.WORLD_HEIGHT/2);

    public static Vector2 coordinatesToScreenProjection(Vector2 coordinates, Vector2 offset, float zoom) {
        // Convert
        var merc = WGS84.wgs84ToMercator(coordinates);
        // Offset
        merc.add(offset);
        // Scale around the screen center (zoom)
        merc.sub(WORLD_CENTER);
        merc.scl(zoom);
        merc.add(WORLD_CENTER);

        return merc;
    }

    public static Polygon polygonToScreenProjection(Polygon polygon, Vector2 offset, float zoom) {
        return Polygon.of(polygon.getPoints().stream()
            .map(point -> coordinatesToScreenProjection(point, offset, zoom))
            .toList());
    }
}
