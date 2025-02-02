package be.sugoi.wopr.programs.thermo.screens;

import be.sugoi.wopr.Main;
import be.sugoi.wopr.programs.thermo.entities.SimulationSpeed;
import be.sugoi.wopr.terminal.InvalidDirective;
import be.sugoi.wopr.terminal.TerminalScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpeedScreen extends TerminalScreen {
    public SpeedScreen(Main g) {
        super(g);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/thermo/speed.txt";
    }

    @Override
    protected void processInput() {
        super.processInput();
        if (!kb.isEnterPressed()) {
            return;
        }
        try {
            int n = Integer.parseInt(kb.buffer());
            g.simulationSpeed = switch (n) {
                case 1 -> SimulationSpeed.REAL_TIME;
                case 2 -> SimulationSpeed.FAST;
                case 3 -> SimulationSpeed.FASTER;
                case 4 -> SimulationSpeed.LIGHTNING_FAST;
                default -> throw new IllegalArgumentException();
            };
            Gdx.input.setInputProcessor(null);

            // Somehow this seems to be the right time to reload scenarios.
            // Doing it in WarScreen::show() would prevent going back from Outcome.
            // Doing it in this screen .show() breaks the transition from WarScreen to here.
            g.setScenarios(g.scenarioMaker.load());

            g.sm().notify(name(), "done");
        } catch (IllegalArgumentException e) {
            // ignore invalid input
        } finally {
            kb.reset();
        }
    }
}
