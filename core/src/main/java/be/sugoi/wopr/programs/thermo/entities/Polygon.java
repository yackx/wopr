package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// A polygon shape.
///
/// Typically used to represent a country.
/// The points forming the polygon can be either WGS84 or screen coordinates.
public class Polygon {
    private final @NotNull List<Vector2> points;

    private Polygon(@NotNull List<Vector2> points) {
        this.points = points;
    }

    public static Polygon of(@NotNull List<Vector2> points) {
        return new Polygon(points);
    }

    /// Returns a list referencing a sub polygon.
    ///
    /// This method is intended to work around a size limitation of
    /// [#polyline(float[])]
    /// @param fromIndex From
    /// @param toIndex To
    /// @return A new [Polygon] containing references to the existing points
    public Polygon subShape(int fromIndex, int toIndex) {
        return Polygon.of(points.subList(fromIndex, toIndex));
    }

    public @NotNull List<Vector2> getPoints() {
        return points;
    }

    public int count() {
        return points.size();
    }
}
