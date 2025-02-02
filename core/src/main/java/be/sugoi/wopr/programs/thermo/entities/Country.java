package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public record Country(
    @NotNull String name,
    @NotNull String countryCode,
    int population,
    @NotNull List<Polygon> polygons,
    @NotNull Vector2 labelPosition)
{
    @Override
    public String toString() {
        var longest = polygons.stream()
            .max(Comparator.comparingInt(Polygon::count))
            .map(Polygon::count)
            .orElse(0);
        return "[" + countryCode + "] " + name + " (" + polygons.size() + ") [" + longest + "] ";
    }
}
