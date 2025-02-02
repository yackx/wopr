package be.sugoi.wopr.programs.thermo.screens;

import be.sugoi.wopr.Main;
import be.sugoi.wopr.terminal.InvalidDirective;
import be.sugoi.wopr.terminal.TerminalScreen;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScenarioDetailsScreen extends TerminalScreen {
    private List<String> scenarioDetailsLines;

    public ScenarioDetailsScreen(Main g) {
        super(g);
    }

    @Override
    public void show() {
        super.show();
        scenarioDetailsLines = buildScenarioDetailsLines(g.getScenario().description());
    }

    private List<String> buildScenarioDetailsLines(String text) {
        List<String> result = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > charsPerRow) {
                result.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
            currentLine.append(word).append(" ");
        }

        if (!currentLine.isEmpty()) {
            result.add(currentLine.toString().trim());
        }

        return result;
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        return switch (directive) {
            case "SCENARIO_TITLE" -> List.of(renderTitle());
            case "SCENARIO_DETAILS" -> renderScenarioDetails();
            case "BELLIGERENTS" -> renderBelligerents();
            default -> throw newInvalidDirective(directive);
        };
    }

    private Label renderTitle() {
        return toLabel(g.getScenario().name(), Align.left, g.theme.color4());
    }

    private List<Label> renderScenarioDetails() {
        return scenarioDetailsLines.stream().map(this::toLabel).toList();
    }

    private List<Label> renderBelligerents() {
        return g.getScenario().parties().stream()
            .map(p -> String.format("  %s (%d)", p.name(), p.nukes().size()))
            .map(it -> toLabel(it, Align.left, g.theme.color6()))
            .toList();
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/thermo/scenario.txt";
    }

    @Override
    protected void processInput() {
        super.processInput();
        if (kb.isEnterPressed()) {
            kb.reset();
            g.sm().notify(name(), "done");
        }
    }
}
