package be.sugoi.wopr;

import be.sugoi.wopr.dm.DisplayModeReplicant;
import be.sugoi.wopr.dm.ScreenResolution;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class Settings {
    private static final String FILEPATH = ".wopr.txt";

    private String theme = "konsole";
    private boolean showCityLabels = true;
    private boolean showTrajectories = true;
    private boolean colorNukesPerParty = true;
    private boolean showDebugScenario = false;
    private boolean skipLogingScreen = false;
    private boolean soundEffects = true;
    private boolean windowed = true;
    private DisplayModeReplicant fullScreenDisplayMode;
    private ScreenResolution windowedScreenResolution;

    private FileHandle createFileHandle() {
        var homeDir = System.getProperty("user.home");
        var filePath = homeDir + "/" + FILEPATH;
        return Gdx.files.absolute(filePath);
    }

    public void load() throws FileNotFoundException {
        var text = loadFile();
        parseRaw(text);
    }

    private String loadFile() throws FileNotFoundException {
        try {
            var handle = createFileHandle();
            return handle.readString();
        } catch (GdxRuntimeException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw (FileNotFoundException) e.getCause();
            }
            throw e;
        }
    }

    public void save() {
        var text = toText();
        saveFile(text);
    }

    private List<String> toText() {
        return List.of(
            "theme=" + theme,
            "war.labels.city=" + showCityLabels,
            "war.nukes.multicolor=" + colorNukesPerParty,
            "war.debug.scenario=" + showDebugScenario,
            "login.skip=" + skipLogingScreen,
            "sounds=" + soundEffects,
            "graphics.windowed=" + windowed,
            "graphics.display_mode=" + fullScreenDisplayMode.toText(),
            "graphics.screen_resolution=" + windowedScreenResolution.toText()
        );
    }

    private void saveFile(List<String> content) {
        var handle = createFileHandle();
        handle.writeString("# WOPR\n", false);
        content.forEach(line -> handle.writeString(line + "\n", true));
    }

    private void parseRaw(String text) {
        var rawSettings = text.split("\n");
        for (var rawSetting : rawSettings) {
            if (rawSetting.startsWith("#")) {
                continue;
            }
            var splitSetting = rawSetting.split("=");
            var k = splitSetting[0].trim();
            var v = splitSetting[1].trim();
            switch (k) {
                case "theme" -> theme = v;
                case "war.labels.city" -> showCityLabels = Boolean.parseBoolean(v);
                case "war.nukes.multicolor" -> colorNukesPerParty = Boolean.parseBoolean(v);
                case "war.debug.scenario" -> showDebugScenario = Boolean.parseBoolean(v);
                case "login.skip" -> skipLogingScreen = Boolean.parseBoolean(v);
                case "sounds" -> soundEffects = Boolean.parseBoolean(v);
                case "graphics.windowed" -> windowed = Boolean.parseBoolean(v);
                case "graphics.display_mode" -> fullScreenDisplayMode = DisplayModeReplicant.fromText(v);
                case "graphics.screen_resolution" -> windowedScreenResolution = ScreenResolution.fromText(v);
                default -> System.err.println("Ignoring setting: " + k);
            }
        }
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setShowCityLabels(boolean showCityLabels) {
        this.showCityLabels = showCityLabels;
    }

    public boolean isShowCityLabels() {
        return showCityLabels;
    }

    public boolean isShowTrajectories() {
        return showTrajectories;
    }

    public void setShowTrajectories(boolean showTrajectories) {
        this.showTrajectories = showTrajectories;
    }

    public boolean isColorNukesPerParty() {
        return colorNukesPerParty;
    }

    public void setColorNukesPerParty(boolean colorNukesPerParty) {
        this.colorNukesPerParty = colorNukesPerParty;
    }

    public boolean isShowDebugScenario() {
        return showDebugScenario;
    }

    public void setShowDebugScenario(boolean showDebugScenario) {
        this.showDebugScenario = showDebugScenario;
    }

    public boolean isSkipLogingScreen() {
        return skipLogingScreen;
    }

    public void setSkipLogingScreen(boolean skipLogingScreen) {
        this.skipLogingScreen = skipLogingScreen;
    }

    public boolean isSoundEffects() {
        return soundEffects;
    }

    public void setSoundEffects(boolean soundEffects) {
        this.soundEffects = soundEffects;
    }

    public Optional<DisplayModeReplicant> getFullScreenDisplayMode() {
        return Optional.ofNullable(fullScreenDisplayMode);
    }

    public void setFullScreenDisplayMode(@NotNull DisplayModeReplicant fullScreenDisplayMode) {
        this.fullScreenDisplayMode = fullScreenDisplayMode;
    }

    public boolean isWindowed() {
        return windowed;
    }

    public void setWindowed(boolean windowed) {
        this.windowed = windowed;
    }

    public Optional<ScreenResolution> getWindowedScreenResolution() {
        return Optional.ofNullable(windowedScreenResolution);
    }

    public void setWindowedScreenResolution(ScreenResolution windowedScreenResolution) {
        this.windowedScreenResolution = windowedScreenResolution;
    }

    public void unsetWindowedScreenResolution() {
        windowedScreenResolution = null;
    }
}
