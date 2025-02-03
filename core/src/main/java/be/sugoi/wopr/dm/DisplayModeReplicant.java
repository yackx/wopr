package be.sugoi.wopr.dm;

import com.badlogic.gdx.Graphics;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/// A subclass of {@link Graphics.DisplayMode}.
///
/// This subclass circumvents the protected constructor of its parent
/// {@link com.badlogic.gdx.Graphics.DisplayMode}
/// and can therefore be instantiated in the app
/// or mocked in unit tests.
public class DisplayModeReplicant extends Graphics.DisplayMode
{
    public DisplayModeReplicant(int width, int height, int refreshRate, int bitsPerPixel) {
        super(width, height, refreshRate, bitsPerPixel);
    }

    public @NotNull String toText() {
        return String.format("%d,%d,%d,%d", width, height, refreshRate, bitsPerPixel);
    }

    public static DisplayModeReplicant fromText(@NotNull String text) {
        var values = Arrays.stream(text.split(",")).map(Integer::parseInt).toList();
        return new DisplayModeReplicant(values.get(0), values.get(1), values.get(2), values.get(3));
    }

    public static DisplayModeReplicant fromDisplayMode(Graphics.DisplayMode displayMode) {
        return new DisplayModeReplicant(
            displayMode.width, displayMode.height, displayMode.refreshRate, displayMode.bitsPerPixel
        );
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(width);
        result = 31 * result + Integer.hashCode(height);
        result = 31 * result + Integer.hashCode(refreshRate);
        result = 31 * result + Integer.hashCode(bitsPerPixel);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof Graphics.DisplayMode that) {
            return width == that.width &&
                height == that.height &&
                refreshRate == that.refreshRate &&
                bitsPerPixel == that.bitsPerPixel;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%dx%d @%d %dbpp (full screen)", width, height, refreshRate, bitsPerPixel);
    }
}
