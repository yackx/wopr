package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.badlogic.gdx.math.MathUtils.random;

public final class City {
    private static final Map<Integer, Float> THRESHOLDS = buildThresholdsMap();
    private static final float DEATH_MAX_FLUCTUATION_RATIO = 0.2f;
    private static final float PREVIOUS_STRIKE_FACTOR = 0.2f;
    private static final float MISS_PROBABILITY = 0.05f;
    private static final float MISS_PENALTY = 0.8f;
    private final @NotNull String name;
    private final @NotNull Vector2 coord;
    private final @NotNull String countryCode;
    private final int population;
    private int fatalities = 0;
    private int hits = 0;

    public City(
        @NotNull String name,
        @NotNull Vector2 coord,
        @NotNull String countryCode,
        int population
    ) {
        this.name = name;
        this.coord = coord;
        this.countryCode = countryCode;
        this.population = population;
    }

    @SuppressWarnings("UnusedReturnValue")
    public int hit() {
        var alive = population - fatalities;
        var killRatio = THRESHOLDS.entrySet().stream()
            .filter(e -> e.getKey() <= alive)
            .findFirst()
            .orElseThrow()
            .getValue();
        var previousStrikesRatio = 1 + PREVIOUS_STRIKE_FACTOR * hits;
        var missPenalty = random() <= MISS_PROBABILITY ? MISS_PENALTY : 1.0f;
        var randomFluctuationRatio = random(1 - DEATH_MAX_FLUCTUATION_RATIO/2, 1 + DEATH_MAX_FLUCTUATION_RATIO/2);
        var fatalities = (int) (alive * killRatio * randomFluctuationRatio / previousStrikesRatio / missPenalty);
        this.fatalities += fatalities;
        hits++;
        return fatalities;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull Vector2 coord() {
        return coord;
    }

    public @NotNull String countryCode() {
        return countryCode;
    }

    public int population() {
        return population;
    }

    public int fatalities() {
        return fatalities;
    }

    public int hits() {
        return hits;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (City) obj;
        return Objects.equals(this.name, that.name) &&
            Objects.equals(this.countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, countryCode);
    }

    @Override
    public String toString() {
        return name + " [" + countryCode + "] @(" + coord + ") p=" + population;
    }

    /**
     * Fatalities thresholds.
     * <p/>
     * Smaller cities suffer a higher percentage of casualties,
     * @return Map key: population threshold, value: casualties ratio
     */
    private static LinkedHashMap<Integer, Float> buildThresholdsMap() {
        LinkedHashMap<Integer, Float> map = new LinkedHashMap<>();
        map.put(10_000_000, 0.02f);
        map.put(5_000_000, 0.04f);
        map.put(1_000_000, 0.15f);
        map.put(500_000, 0.25f);
        map.put(100_000, 0.40f);
        map.put(50_000, 0.50f);
        map.put(10_000, 0.75f);
        map.put(0, 0.9f);
        return map;
    }
}
