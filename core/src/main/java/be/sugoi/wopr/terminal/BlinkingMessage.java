package be.sugoi.wopr.terminal;

import be.sugoi.wopr.programs.thermo.entities.Updateable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlinkingMessage implements Updateable {
    private @NotNull String message = "";
    private boolean persistent = false;
    private float clock;

    public void set(@NotNull String message, boolean persistent) {
        this.message = message;
        this.persistent = persistent;
        this.clock = 0.0f;
    }

    public void setMessage(@NotNull String message) {
        set(message, false);
    }

    public void setPersistentMessage(@NotNull String message) {
        set(message, true);
    }

    public Optional<String> message() {
        if (clock > 2.0f) {
            return persistent ? Optional.of(message) : Optional.empty();
        }
        var decimal = clock - (int) clock;
        return decimal < 0.35f || (decimal >= 0.50f && decimal < 0.85f) ? Optional.of(message) : Optional.empty();
    }

    @Override
    public float update(float delta) {
        clock += delta;
        return clock;
    }
}
