package be.sugoi.wopr.dm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

public class DisplayManager {
    private static final Logger logger = Logger.getLogger(DisplayManager.class.getName());

    public static @NotNull List<Graphics.DisplayMode> listDisplayModes() {
        return Arrays.stream(Gdx.graphics.getDisplayModes())
            .collect(groupingBy(
                mode -> mode.width + "x" + mode.height + ", bpp: " + mode.bitsPerPixel,
                maxBy(comparingInt(m -> m.refreshRate))
            ))
            .values()
            .stream()
            .map(Optional::orElseThrow)
            .toList();
    }

    public static @NotNull Graphics.DisplayMode getBestDisplayMode(@NotNull List<Graphics.DisplayMode> displayModes) {
        return displayModes.stream()
            .max(comparingInt((Graphics.DisplayMode dm) -> dm.width)
                .thenComparing(dm -> dm.refreshRate))
            .orElseThrow();
    }

    public static @NotNull Graphics.DisplayMode getSafestDisplayMode(@NotNull List<Graphics.DisplayMode> displayModes) {
        return displayModes.stream()
            .min(comparingInt((Graphics.DisplayMode dm) -> dm.width)
                .thenComparing(comparingInt((Graphics.DisplayMode dm) -> dm.refreshRate).reversed()))
            .orElseThrow();
    }

    public static void changeDisplayMode(@NotNull DisplayModeReplicant newMode) {
        var mode = listDisplayModes().stream()
            .filter(newMode::equals)
            .findFirst()
            .orElseThrow();
        changeDisplayMode(mode);
    }

    public static void changeDisplayMode(@NotNull Graphics.DisplayMode newMode) {
        logger.info("Change display mode to " + newMode);
        Gdx.graphics.setFullscreenMode(newMode);
    }

    public static void changeScreenResolution(@NotNull ScreenResolution screenResolution) {
        logger.info("Change screen resolution to " + screenResolution);
        Gdx.graphics.setWindowedMode(screenResolution.width(), screenResolution.height());
    }
}
