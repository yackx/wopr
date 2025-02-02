package be.sugoi.wopr.terminal;

import be.sugoi.wopr.Main;
import be.sugoi.wopr.utils.Pair;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends TerminalScreen {
    public SettingsScreen(Main g) {
        super(g);
    }

    @Override
    public void show() {
        super.show();
        try {
            g.settings.load();
        } catch (FileNotFoundException e) {
            // ignore
        }
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        return switch (directive) {
            case "GENERAL_SETTINGS" -> renderGeneralSettings();
            case "WAR_SETTINGS" -> renderWarSettings();
            default -> throw newInvalidDirective(directive);
        };
    }

    private List<Label> renderGeneralSettings() {
        var data = List.of(
            Pair.of("Skip login screen", booleanToString(g.settings.isSkipLogingScreen())),
            Pair.of("Sound effects", booleanToString(g.settings.isSoundEffects()))
        );
        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            var elt = data.get(i);
            labels.add(toLabel(formatSetting(i+1, elt.first(), elt.second())));
        }
        return labels;
    }

    private List<Label> renderWarSettings() {
        var data = List.of(
            Pair.of("Show city labels", booleanToString(g.settings.isShowCityLabels())),
            Pair.of("Nuke color per party", booleanToString(g.settings.isColorNukesPerParty())),
            Pair.of("Show debug scenario", booleanToString(g.settings.isShowDebugScenario()))
        );
        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            var elt = data.get(i);
            labels.add(toLabel(formatSetting(i+3, elt.first(), elt.second())));
        }
        return labels;
    }

    private String booleanToString(boolean value) {
        return value ? "ON" : "OFF";
    }

    private String formatSetting(int position, String label, String value) {
        return String.format("  %d. %-30s%30s", position, label, value);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value)
        throws InvalidDirective
    {
        throw newInvalidDirective(directive, value);
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/settings.txt";
    }

    @Override
    protected void processInput() {
        super.processInput();
        if (!kb.isEnterPressed()) {
            return;
        }
        try {
            int n = Integer.parseInt(kb.buffer());
            switch (n) {
                case 1 -> g.settings.setSkipLogingScreen(!g.settings.isSkipLogingScreen());
                case 2 -> g.settings.setSoundEffects(!g.settings.isSoundEffects());
                case 3 -> g.settings.setShowCityLabels(!g.settings.isShowCityLabels());
                case 4 -> g.settings.setColorNukesPerParty(!g.settings.isColorNukesPerParty());
                case 5 -> g.settings.setShowDebugScenario(!g.settings.isShowDebugScenario());
                default -> throw new NumberFormatException();
            }
            g.settings.save();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            feedbackMessage.setMessage("Invalid entry");
        } finally {
            kb.reset();
        }
    }
}
