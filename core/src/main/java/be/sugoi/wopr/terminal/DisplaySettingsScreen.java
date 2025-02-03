package be.sugoi.wopr.terminal;

import be.sugoi.wopr.Main;
import be.sugoi.wopr.dm.DisplayManager;
import be.sugoi.wopr.dm.DisplayModeReplicant;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class DisplaySettingsScreen extends TerminalScreen {
    private List<Graphics.DisplayMode> displayModes;

    public DisplaySettingsScreen(Main g) {
        super(g);
    }

    @Override
    public void show() {
        super.show();
        displayModes = DisplayManager.listDisplayModes();
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/dm.txt";
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        return switch (directive) {
            case "DISPLAY_MODES" -> renderDisplayModes();
            case "CURRENT" -> List.of(renderCurrentDisplayMode());
            default -> throw newInvalidDirective(directive);
        };
    }

    private List<Label> renderDisplayModes() {
        BiFunction<Integer, Graphics.DisplayMode, String> stringify = (i, dm) ->
            String.format("   %d. %dx%d @%dHz", (i + 1), dm.width, dm.height, dm.refreshRate);
        return IntStream.range(0, displayModes.size())
            .mapToObj(i -> stringify.apply(i, displayModes.get(i)))
            .map(this::toLabel)
            .toList();
    }

    private String buildDisplayResolutionString() {
        if (g.settings.isWindowed()) {
            var screenResolution = g.settings.getWindowedScreenResolution().orElseThrow();
            return String.format("%dx%d (window)", screenResolution.width(), screenResolution.height());
        } else {
            var dm = g.settings.getFullScreenDisplayMode().orElseThrow();
            return String.format("%dx%d @%dHz (full screen)", dm.width, dm.height, dm.refreshRate);
        }
    }

    private Label renderCurrentDisplayMode() {
        return toLabel("Current: " + buildDisplayResolutionString());
    }

    @Override
    protected void processInput() {
        super.processInput();
        if (!kb.isEnterPressed()) {
            return;
        }
        if ("w".equalsIgnoreCase(kb.buffer())) {
            g.settings.setWindowed(true);
            g.setGraphics();
        }
        try {
            int n = Integer.parseInt(kb.buffer());
            var dm = displayModes.get(n-1);
            g.settings.setFullScreenDisplayMode(DisplayModeReplicant.fromDisplayMode(dm));
            g.settings.setWindowed(false);
            g.setGraphics();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            feedbackMessage.setMessage("Invalid entry");
        } finally {
            kb.reset();
        }
    }
}
