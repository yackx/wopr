package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A nuclear missile launch site.
 * <p/>
 * Can be a submarine or a facility, in the loose sense,
 * where several silos are grouped as one.
 */
public final class LaunchSite {
    public enum Type {
        FACILITY, SUBMARINE, MOBILE;

        public static Type fromString(String s) {
            return switch (s) {
                case "submarine" -> Type.SUBMARINE;
                case "facility" -> Type.FACILITY;
                case "mobile" -> Type.MOBILE;
                default -> throw new IllegalArgumentException("Unknown type for: " + s);
            };
        }
    }

    private final @NotNull String name;
    private final @NotNull String countryCode;
    private final @NotNull Vector2 coord;
    private final @NotNull Type type;
    private int remainingCapacity;

    public LaunchSite(
        @NotNull String name,
        @NotNull String countryCode,
        @NotNull Vector2 coord,
        @NotNull Type type
    ) {
        if (countryCode.length() != 2) {
            throw new IllegalArgumentException("Invalid country code: " + countryCode);
        }
        this.name = name;
        this.countryCode = countryCode;
        this.coord = coord;
        this.type = type;
        this.remainingCapacity = 0;
    }

    public void decrementCapacity() {
        assert remainingCapacity > 0;
        remainingCapacity--;
    }

    public boolean isEmpty() {
        return remainingCapacity == 0;
    }

    public void setRemainingCapacity(int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public @NotNull Type getType() {
        return type;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String countryCode() {
        return countryCode;
    }

    public @NotNull Vector2 coord() {
        return coord;
    }

    public int remainingCapacity() {
        return remainingCapacity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LaunchSite) obj;
        return Objects.equals(this.name, that.name) &&
            Objects.equals(this.countryCode, that.countryCode) &&
            Objects.equals(this.coord, that.coord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, countryCode, coord);
    }

    @Override
    public String toString() {
        return name + " (" + countryCode + ") @ " + coord;
    }
}
