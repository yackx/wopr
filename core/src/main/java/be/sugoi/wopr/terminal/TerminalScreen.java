package be.sugoi.wopr.terminal;

import be.sugoi.wopr.GdxFontHelper;
import be.sugoi.wopr.Main;
import be.sugoi.wopr.input.KeyboardBuffer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/// Simulates a computer terminal.
///
/// The content of the screen is loaded from a template.
/// Rendering takes place by issuing [Label] objects.
/// Simple directives such as `{HALIGN}` are substituted with the appropriate value.
///
/// Implementing classes perform further placeholder substitutions.
abstract public class TerminalScreen extends BaseScreen {
    protected final Main g;
    protected int currentLabelIndex;
    protected float rowHeight;
    protected float rowWidth;
    protected float charWidth;
    protected int charsPerRow;
    protected final KeyboardBuffer kb;

    private List<String> template;

    private static final float LOAD_SCREEN_CHAR_INTERVAL = 0.0025f;
    private int numberOfCharsPrinted;
    protected float clock;
    protected final BlinkingMessage feedbackMessage;
    private final Sound typingSound;

    public TerminalScreen(Main g) {
        this.g = g;
        kb = new KeyboardBuffer();
        feedbackMessage = new BlinkingMessage();
        typingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/loading/loading.mp3"));
    }

    /// Render a directive without a value.
    ///
    /// @param directive Example: `{FOO}`.
    /// @return A list of [Label]'s
    /// @throws InvalidDirective If the directive is unknown.
    /// @see #toLabel(String)
    abstract protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective;

    /// Render a directive with a value.
    ///
    /// @param directive Example: `{FOO:BAR}`.
    /// @return A list of [Label]'s
    /// @throws InvalidDirective If the directive is unknown.
    /// @see #toLabel(String)
    abstract protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value)
        throws InvalidDirective;

    /// Create a uniformly formatted [InvalidDirective] instance
    ///
    /// @param directive Unknown directive
    /// @return Exception to throw
    protected InvalidDirective newInvalidDirective(String directive) {
        return new InvalidDirective(String.format("Unknown directive {%s}", directive));
    }

    /// Create a uniformly formatted [InvalidDirective] instance
    ///
    /// @param directive Unknown directive
    /// @param value     Directive value
    /// @return Exception to throw
    protected InvalidDirective newInvalidDirective(String directive, String value) {
        return new InvalidDirective(String.format("Unknown directive {%s:%s}", directive, value));
    }

    /// Path to the template
    ///
    /// @return Internal path
    abstract protected @NotNull String templatePath();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(kb);
        g.fm.setScaleForAll(g.screenViewport.getWorldHeight() / Main.SCREEN_HEIGHT);
        computeFontDimensions();
        template = loadTemplate(templatePath());
        clock = 0;
        if (g.settings.isSoundEffects()) {
            typingSound.play();
        }
    }

    private void computeFontDimensions() {
        var font = getFont();
        rowHeight = GdxFontHelper.getHeight(font) * 2f;
        charWidth = GdxFontHelper.getWidth(font);
        rowWidth = Gdx.graphics.getWidth();
        charsPerRow = (int) (rowWidth / charWidth);
    }

    private List<String> loadTemplate(String templatePath) {
        var text = Gdx.files.internal(templatePath).readString();
        return Arrays.stream(text.split("\n")).toList();
    }

    private BitmapFont getFont() {
        return g.fm.getFont(Main.FONT_M);
    }

    protected Label.LabelStyle createBaseStyle() {
        var style = new Label.LabelStyle();
        style.fontColor = g.theme.color1();
        style.font = getFont();
        return style;
    }

    @Override
    public void render(float delta) {
        clock += delta;
        feedbackMessage.update(delta);
        ScreenUtils.clear(g.theme.background());
        g.screenViewport.apply();
        g.batch.setProjectionMatrix(g.camera.combined);
        var labels = applyTemplate();
        processInput();
        renderText(labels);
    }

    protected void processInput() {
        // Erase buffer if Esc and not empty
        if (kb.isEscapePressed() && !kb.buffer().isEmpty()) {
            kb.reset();
            return;
        }

        // F7 - Theme
        if (kb.getSpecialKey() == Input.Keys.F7) {
            g.sm().notify(name(), "theme");
            return;
        }

        // F8 - Settings
        if (kb.getSpecialKey() == Input.Keys.F8) {
            g.sm().notify(name(), "settings");
            return;
        }

        // F10 - Exit
        if (kb.getSpecialKey() == Input.Keys.F10) {
            g.sm().notify(name(), "exit");
            return;
        }

        // Esc - go back
        if (kb.isEscapePressed()) {
            Gdx.input.setInputProcessor(null);
            g.sm().notify(name(), "back");
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    private int computeProgressiveCharIndex() {
        return (int) (clock / LOAD_SCREEN_CHAR_INTERVAL);
    }

    protected Label renderBlankLine() {
        return toLabel("");
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private List<Label> applyTemplate() {
        var directives1 = Pattern.compile("\\{(\\w+):(\\w+)}");
        var directives0 = Pattern.compile("\\{(\\w+)}");

        List<Label> allLabels = new ArrayList<>();
        for (var line : template) {
            List<Label> lineLabels = List.of();
            var style = createBaseStyle();
            var align = Align.left;

            var matcher1 = directives1.matcher(line);
            if (matcher1.find()) {
                var directive = matcher1.group(1);
                var value = matcher1.group(2);
                switch (directive) {
                    case "HALIGN" -> {
                        switch (value) {
                            case "LEFT" -> {
                            }
                            case "CENTER" -> align = Align.center;
                            default -> throw newInvalidDirective(directive, value);
                        }
                    }
                    case "VALIGN" -> {
                        switch (value) {
                            case "BOTTOM" -> align = Align.bottom;
                            default -> throw newInvalidDirective(directive, value);
                        }
                    }
                    case "COLOR" -> {
                        switch (value) {
                            case "2" -> style.fontColor = g.theme.color2();
                            case "3" -> style.fontColor = g.theme.color3();
                            default -> throw newInvalidDirective(directive, value);
                        }
                    }
                    default -> lineLabels = renderDirective(directive, value);
                }
                line = line.replaceAll("\\{.*?}", "");
            }

            var matcher0 = directives0.matcher(line);
            if (matcher0.find()) {
                var key = matcher0.group(1);
                switch (key) {
                    case "BUFFER" -> {
                        var buffer = kb.buffer();
                        line = line.replaceAll("\\{.*?}", buffer);
                    }
                    case "HEADER" -> {
                        align = Align.center;
                        style.fontColor = g.theme.color2();
                        line = line.replaceAll("\\{.*?}", "WOPR v.20250127 - (P)&(C) 2025 @yackx - All rights reserved");
                    }
                    case "BOTTOM_BAR" -> {
                        align = Align.bottom;
                        style.fontColor = g.theme.color1();
                        line = "ESC=Back  F1=Help  F7=Theme  F8=Settings F10=Exit";
                    }
                    case "ERROR_MESSAGE" -> {
                        var s = feedbackMessage.message().orElse("");
                        lineLabels = List.of(toLabel(s, Align.left, g.theme.danger()));
                    }
                    default -> lineLabels = renderDirective(key);
                }
                line = line.replaceAll("\\{.*?}", "");
            }

            if (lineLabels.isEmpty()) {
                var label = toLabel(line, align, style);
                lineLabels = List.of(label);
            }

            allLabels.addAll(lineLabels);
        }

        return allLabels;
    }

    private void renderText(List<Label> labels) {
        g.stage.clear();
        numberOfCharsPrinted = 0;
        currentLabelIndex = 0;
        var totalCharsCount = labels.stream().mapToInt(label -> label.getText().length()).sum();
        for (var label : labels) {
            var count = renderLabel(label);
            currentLabelIndex++;
            numberOfCharsPrinted += count;
            if (numberOfCharsPrinted >= totalCharsCount) {
                typingSound.stop();
            }
        }

        g.stage.act();
        g.stage.draw();
    }

    protected Label toLabel(String line, int align, Label.LabelStyle style) {
        var label = new Label(line, style);
        label.setSize(Gdx.graphics.getWidth(), rowHeight);
        var margin = align == Align.left ? 2f : 0f;
        label.setX(charWidth * margin);
        label.setAlignment(align);
        return label;
    }

    @SuppressWarnings("SameParameterValue")
    protected Label toLabel(String line, int align, Color color) {
        var style = createBaseStyle();
        style.fontColor = color;
        return toLabel(line, align, style);
    }

    protected Label toLabel(String line, int align) {
        return toLabel(line, align, createBaseStyle());
    }

    @SuppressWarnings("SameParameterValue")
    protected Label toLabel(String line, Color color) {
        return toLabel(line, Align.left, color);
    }

    protected Label toLabel(String line) {
        return toLabel(line, Align.left, createBaseStyle());
    }

    protected int renderLabel(Label label) {
        // Adjust text to print to achieve a typing effect
        var progressiveDisplayCharIndex = computeProgressiveCharIndex();
        var numberOfCharsToPrint = progressiveDisplayCharIndex - numberOfCharsPrinted;
        assert numberOfCharsToPrint >= 0;

        var text = label.getText();
        if (numberOfCharsToPrint < text.length()) {
            var s = text.substring(0, numberOfCharsToPrint);
            label.setText(s);
        }

        var pseudoAlign = label.getLabelAlign();
        var y = pseudoAlign == Align.bottom ? rowHeight : Gdx.graphics.getHeight() - (currentLabelIndex + 1) * rowHeight;
        label.setY(y);
        var actualAlign = pseudoAlign == Align.bottom ? Align.center : pseudoAlign;
        label.setAlignment(actualAlign);
        g.stage.addActor(label);

        return text.length();
    }

    @Override
    public void resize(int width, int height) {
        g.screenViewport.update(width, height, true);
        g.fm.setScaleForAll(g.screenViewport.getWorldHeight() / Main.SCREEN_HEIGHT);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        typingSound.stop();
        typingSound.dispose();
    }
}
