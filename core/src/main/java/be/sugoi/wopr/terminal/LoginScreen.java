package be.sugoi.wopr.terminal;

import be.sugoi.wopr.Main;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LoginScreen extends TerminalScreen {
    private boolean feedbackMessageSet;

    public LoginScreen(Main g) {
        super(g);
    }

    @Override
    public void show() {
        super.show();
        feedbackMessage.setPersistentMessage("");
        feedbackMessageSet = false;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (clock > 5.0f) {
            g.sm().notify(name(), "done");
        }
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        return List.of(switch (directive) {
            case "CONNECTING" -> renderConnecting();
            case "CONNECTED" -> renderConnected();
            default -> throw newInvalidDirective(directive);
        });
    }

    private Label renderConnecting() {
        if (clock > 1.0f && !feedbackMessageSet) {
            feedbackMessage.setPersistentMessage("Connecting...");
            feedbackMessageSet = true;
        }
        return toLabel(feedbackMessage.message().orElse(""));
    }

    private Label renderConnected() {
        if (clock > 3.5f) {
            return toLabel("Success", g.theme.color3());
        }
        return toLabel("");
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive, value);
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/login.txt";
    }
}
