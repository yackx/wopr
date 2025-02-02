package be.sugoi.wopr.theme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Theme {
    public enum Modifier {
        NORMAL(1.0f), MUTE(0.7f);

        private final float alphaComponent;

        Modifier(float alphaComponent) {
            this.alphaComponent = alphaComponent;
        }
    }

    private final @NotNull Map<String, Color> theme;
    private final @NotNull String name;

    public Theme(@NotNull String name) {
        this.name = name;
        theme = load();
    }

    private Map<String, Color> load() {
        var handle = Gdx.files.internal("themes/" + name + ".txt");
        var lines = handle.readString().split("\n");
        return Arrays.stream(lines)
            .filter(it -> !it.startsWith("#"))
            .filter(it -> !it.isBlank())
            .map(line -> line.split("="))
            .collect(Collectors.toMap(
                parts -> parts[0].trim(),
                parts -> new Color(rgbStringToInt(parts[1].trim())
                )));
    }

    private static int rgbStringToInt(String rgb) {
        if (rgb.startsWith("#")) {
            rgb = rgb.substring(1);
        }
        int r = Integer.parseInt(rgb.substring(0, 2), 16);
        int g = Integer.parseInt(rgb.substring(2, 4), 16);
        int b = Integer.parseInt(rgb.substring(4, 6), 16);
        int a = rgb.length() == 8 ? Integer.parseInt(rgb.substring(6, 8), 16) : 255;
        return (r << 24) | (g << 16) | (b << 8) | a;
    }

    private @NotNull Color color(@SuppressWarnings("SameParameterValue") String colorKey, Modifier modifier) {
        var color = theme.get(colorKey);
        return new Color(color.r, color.g, color.b, modifier.alphaComponent);
    }

    public @NotNull Color color1() {
        return theme.get("color1");
    }

    public @NotNull Color color1(@NotNull Modifier modifier) {
        return color("color1", modifier);
    }

    public @NotNull Color accentColor() {
        return theme.get("accent");
    }

    public @NotNull Color color2() {
        return theme.get("color2");
    }

    public @NotNull Color color3() {
        return theme.get("color3");
    }

    public @NotNull Color color4() {
        return theme.get("color4");
    }

    public @NotNull Color color5() {
        return theme.get("color5");
    }

    public @NotNull Color color6() {
        return theme.get("color6");
    }

    @SuppressWarnings("unused")
    public @NotNull Color colorN(int n) {
        if (n < 1 || n > 6) {
            throw new IllegalArgumentException("Illegal color: " + n);
        }
        return theme.get("color" + n);
    }

    public @NotNull Color danger() {
        return theme.get("danger");
    }

    public @NotNull Color background() {
        return theme.get("background");
    }

    public @NotNull String name() {
        return name;
    }
}
