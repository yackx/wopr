package be.sugoi.wopr.terminal;

import be.sugoi.wopr.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.OrderedMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ThemeScreen extends TerminalScreen {
    private List<String> themeNames;
    private OrderedMap<String, Color> sample;

    public ThemeScreen(Main g) {
        super(g);
    }

    @Override
    public void show() {
        super.show();
        themeNames = loadThemeNames();
        sample = buildSample();
    }

    private List<String> loadThemeNames() {
        var handle = Gdx.files.internal("themes");
        return Arrays.stream(handle.list())
                .map(FileHandle::name)
                .map(name -> name.replaceAll(".txt", ""))
                .toList();
    }

    private OrderedMap<String, Color> buildSample() {
        var ordered = new OrderedMap<String, Color>();
        ordered.put("color1", g.theme.color1());
        ordered.put("color2", g.theme.color2());
        ordered.put("color3", g.theme.color3());
        ordered.put("color4", g.theme.color4());
        ordered.put("color5", g.theme.color5());
        ordered.put("color6", g.theme.color6());
        ordered.put("accent", g.theme.accentColor());
        ordered.put("danger", g.theme.danger());
        ordered.put("background", g.theme.background());
        return ordered;
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/theme.txt";
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        return switch (directive) {
            case "THEMES" -> renderListThemes();
            case "CURRENT_THEME_NAME" -> List.of(renderCurrentTheme());
            case "CURRENT_SAMPLE" -> renderCurrentSample();
            default -> throw newInvalidDirective(directive);
        };
    }

    private List<Label> renderListThemes() {
        return IntStream.range(0, themeNames.size())
            .mapToObj(i -> "  " + (i + 1) + ". " + themeNames.get(i))
            .map(this::toLabel)
            .toList();
    }

    private Label renderCurrentTheme() {
        var str = "Current theme: " + g.theme.name();
        return toLabel(str, Align.left, g.theme.accentColor());
    }

    private List<Label> renderCurrentSample() {
        List<Label> labels = new ArrayList<>();
        for (var entry : sample) {
            labels.add(toLabel("  " + entry.key, Align.left, entry.value));
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
            g.changeTheme(themeNames.get(n - 1));
            sample = buildSample();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // ignore invalid input
        } finally {
            kb.reset();
        }
    }
}
