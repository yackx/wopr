package be.sugoi.wopr.terminal;

import be.sugoi.wopr.Main;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProgramScreen extends TerminalScreen {
    public ProgramScreen(Main g) {
        super(g);
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/program.txt";
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
    protected void processInput() {
        super.processInput();
        if (!kb.isEnterPressed()) {
            return;
        }
        try {
            int n = Integer.parseInt(kb.buffer());
            switch (n) {
                case 3 -> g.sm().notify(name(), "war");
                case 1, 2 -> feedbackMessage.setMessage("Not available");
                default -> throw new NumberFormatException();
            }
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
