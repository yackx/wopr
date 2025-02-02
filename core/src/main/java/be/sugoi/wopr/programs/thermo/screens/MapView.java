package be.sugoi.wopr.programs.thermo.screens;

import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

/// A view of the map.
///
/// Each view has an offset from `(0,0)` and a zoom level.
/// Thresholds allow to declutter some maps.
/// For instance, due to the existence of many very large cities,
/// China requires a higher population threshold.
public enum MapView {
    WORLD(new Vector2(0.0f, -0.15f), 1.0f, 0.002f, 10_000_000, 10_000_000, 50_000_000, 0.5f),
    EUROPE(new Vector2(0.065f, -0.18f), 6.5f, 0.003f, 500_000, 1_000_000, 3_000_000, 1.0f),
    RUSSIA(new Vector2(-0.12f, -0.22f), 3.10f, 0.003f, 500_000, 2_000_000, 3_000_000, 0.8f),
    NORTH_AMERICA(new Vector2(0.425f, -0.18f), 3.0f, 0.002f, 500_000, 2_000_000, 5_000_000, 0.8f),
    CHINA(new Vector2(-0.18f, -0.12f), 6.0f, 0.002f, 1_000_000, 10_000_000, 2_000_000, 1.0f);

    final Vector2 offset;
    final float zoomFactor;
    final float citySquareSize;
    final int cityPopulationThresholdMinimum;
    final int cityPopulationThresholdLarge;
    final int countryPopulationThresholdLarge;
    final float detonationFactor;

    MapView(
        Vector2 offset,
        float zoomFactor,
        float citySquareSize,
        int cityPopulationThresholdMinimum,
        int cityPopulationThresholdLarge,
        int countryPopulationThresholdLarge,
        float detonationFactor)
    {
        this.offset = offset;
        this.zoomFactor = zoomFactor;
        this.citySquareSize = citySquareSize;
        this.cityPopulationThresholdMinimum = cityPopulationThresholdMinimum;
        this.cityPopulationThresholdLarge = cityPopulationThresholdLarge;
        this.countryPopulationThresholdLarge = countryPopulationThresholdLarge;
        this.detonationFactor = detonationFactor;
    }

    public static MapView getByName(String name) {
        return Arrays.stream(MapView.values())
            .filter(v -> v.name().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No enum " + MapView.class.getCanonicalName() + ":" + name));
    }
}
