package be.sugoi.wopr.dm;

import java.util.Arrays;


// Screen resolution in windowed mode
public record ScreenResolution(int width, int height) {
    public String toText() {
        return width + "," + height;
    }

    public static ScreenResolution fromText(String text) {
        var split = Arrays.stream(text.split(",")).map(Integer::parseInt).toList();
        return new ScreenResolution(split.get(0), split.get(1));
    }

    @Override
    public String toString() {
        return width + "x" + height + " (windowed)";
    }
}
