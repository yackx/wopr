package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.math.MathUtils;
import org.jetbrains.annotations.NotNull;

public class Detonation implements Updateable {

    // Three animation phases:
    // 1. Expansion (circle is growing)
    public static final float GROWTH_DURATION = 1.0f;
    // 2. Peak (full light intensity, full diameter)
    public static final float FULL_PEAK_DURATION = 3.0f;
    // 3. Decay (circle is shrinking, intensity diminishes)
    public static final float DECAY_DURATION = 3.0f;

    private final @NotNull City city;
    private final float speed;
    private float clock;

    public Detonation(@NotNull City city, float speed) {
        this.city = city;
        this.speed = speed;
    }

    @Override
    public float update(float delta) {
        return clock += delta;
    }

    public float diameter() {
        if (clock < GROWTH_DURATION * speed) {
            return MathUtils.lerp(0.0f, 1.0f, clock / GROWTH_DURATION * speed);
        }
        return 1.0f;
    }

    public float lightIntensity() {
        float growthPeakDuration = (GROWTH_DURATION + FULL_PEAK_DURATION) * speed;
        float totalDuration = (GROWTH_DURATION + FULL_PEAK_DURATION + DECAY_DURATION) * speed;

        if (clock <= growthPeakDuration) {
            return 1.0f;  // full intensity
        }
        if (clock <= totalDuration) {
            float alpha = (clock - growthPeakDuration) / (totalDuration - growthPeakDuration);
            return MathUtils.lerp(1.0f, 0.0f, alpha);
        }
        return 0.0f;
    }

    public @NotNull City city() {
        return city;
    }
}
