package be.sugoi.wopr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/// Manages font in the application.
///
/// The game uses the [GDX Freetype](https://libgdx.com/wiki/extensions/gdx-freetype) extension.
/// It leverages .ttf files and generates {@link BitmapFont} on the fly.
///
/// This utility class encapsulates and caches the generated fonts.
/// It offers convenience methods to regenerate them when they need to be resized.
///
/// Callers should call {@link #dispose()} and {@link #disposeAllFonts()} appropriately.
public class FontManager implements Disposable {

    /// A {@link BitmapFont} with associated information (name, size, ttf).
    private record FontWithInfo(
        BitmapFont font,
        String ttf,
        String name,
        int size,
        FreeTypeFontGenerator.FreeTypeFontParameter parameter)
    {
        // empty
    }

    private final String fontFolder;
    private final @NotNull Map<String, FreeTypeFontGenerator> generators = new HashMap<>();
    private final @NotNull Map<String, FontWithInfo> fonts = new HashMap<>();

    /// Current scale, relative to the original.
    private float currentScale;

    public FontManager(String fontFolder) {
        this.fontFolder = fontFolder;
        this.currentScale = Float.NaN;
    }

    public void addTrueTypeFont(String ttf) {
        if (generators.containsKey(ttf)) {
            return;
        }
        generators.put(ttf, new FreeTypeFontGenerator(Gdx.files.internal(fontFolder + "/" + ttf)));
    }

    public BitmapFont newFont(
        String ttf, String name, int size, float scale, FreeTypeFontGenerator.FreeTypeFontParameter parameter
    ) {
        // TODO what if font already exists?
        parameter.size = size;
        parameter.characters += "█┌┐└┘│─├┤┬┴⫽";
        BitmapFont font;
        font = generators.get(ttf).generateFont(parameter);
        font.setUseIntegerPositions(false);
        font.getData().setScale(scale);
        var fontWithInfo = new FontWithInfo(font, ttf, name, size, parameter);
        fonts.put(name, fontWithInfo);
        return fontWithInfo.font();
    }

    /// Create a new bitmap font
    ///
    /// @param ttf TTF file name
    /// @param name Font name (identifier in the game)
    /// @param size Size
    /// @param scale Font scale. Start with `1.0f`.
    @SuppressWarnings("UnusedReturnValue")
    public BitmapFont newFont(String ttf, String name, int size, float scale) {
        return newFont(ttf, name, size, scale,  new FreeTypeFontGenerator.FreeTypeFontParameter());
    }

    /// Set the scale for all fonts.
    ///
    /// If the new scale is different than the current one,
    /// all fonts will be regenerated.
    ///
    /// @param scaleXY Scale (both X and Y)
    public void setScaleForAll(float scaleXY) {
        if (scaleXY == currentScale || scaleXY == 0.0f) {
            return;
        }
        currentScale = scaleXY;
        var fontEntries = new ArrayList<>(fonts.values());
        disposeAllFonts();
        fonts.clear();
        fontEntries.forEach(fwi -> newFont(fwi.ttf(), fwi.name(), fwi.size(), scaleXY, fwi.parameter()));
    }

    public BitmapFont getFont(String name) {
        return fonts.get(name).font();
    }

    private void disposeAllFonts() {
        fonts.values().stream().map(FontWithInfo::font).forEach(BitmapFont::dispose);
    }

    public void disposeFont(String name) {
        getFont(name).dispose();
    }

    @Override
    public void dispose() {
        disposeAllFonts();
        generators.values().forEach(FreeTypeFontGenerator::dispose);
        generators.clear();
    }
}
