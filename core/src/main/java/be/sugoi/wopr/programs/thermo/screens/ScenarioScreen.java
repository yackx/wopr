package be.sugoi.wopr.programs.thermo.screens;

import be.sugoi.wopr.Main;
import be.sugoi.wopr.terminal.InvalidDirective;
import be.sugoi.wopr.terminal.TerminalScreen;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScenarioScreen extends TerminalScreen {
    public ScenarioScreen(Main g) {
        super(g);
    }

    @Override
    public void show() {
        super.show();
        g.setScenarios(g.scenarioMaker.load());
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/thermo/scenarios.txt";
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        if (!"SCENARIOS".equals(directive)) {
            throw newInvalidDirective(directive);
        }
        List<Label> labels = new ArrayList<>();
        for (int i = 0; i < g.getScenarios().size(); i++) {
            String name = g.getScenarios().get(i).name();
            if (g.settings.isShowDebugScenario() || !name.toLowerCase().startsWith("debug")) {
                var output = "  " + (i + 1) + ". " + g.getScenarios().get(i).name();
                labels.add(toLabel(output, Align.left));
            }
        }
        return labels;
    }

    @Override
    protected void processInput() {
        super.processInput();
        if (!kb.isEnterPressed()) {
            return;
        }
        try {
            int n = Integer.parseInt(kb.buffer());
            g.setScenarioIndex(n - 1);
            g.sm().notify(name(), "done");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            feedbackMessage.setMessage("Invalid entry");
        } finally {
            kb.reset();
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }
}
