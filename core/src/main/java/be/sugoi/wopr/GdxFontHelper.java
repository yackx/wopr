package be.sugoi.wopr;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;

public class GdxFontHelper {
    private static final String FULL_BLOCK = "█";

    private GdxFontHelper() {
        // empty
    }

    public static float getHeight(BitmapFont font, String text) {
        final var layout = new GlyphLayout(font, text);
        return layout.height;
    }

    public static float getHeight(BitmapFont font) {
        return getHeight(font, FULL_BLOCK);
    }


    public static float getWidth(BitmapFont font, String text) {
        final var layout = new GlyphLayout(font, text);
        return layout.width;
    }

    public static float getWidth(BitmapFont font) {
        return getWidth(font, FULL_BLOCK);
    }

    public static Vector2 getDimensions(BitmapFont font, String text) {
        return new Vector2(getWidth(font, text), getHeight(font, text));
    }

    public static Vector2 getDimensions(BitmapFont font) {
        return new Vector2(getWidth(font, FULL_BLOCK), getHeight(font, FULL_BLOCK));
    }

    public static float centerX(BitmapFont font, String text, float worldWidth) {
        final var layout = new GlyphLayout(font, text);
        return (worldWidth - layout.width) / 2;
    }
}
