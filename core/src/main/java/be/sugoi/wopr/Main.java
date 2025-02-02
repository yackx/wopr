package be.sugoi.wopr;

import be.sugoi.wopr.programs.thermo.entities.Cities;
import be.sugoi.wopr.programs.thermo.entities.Countries;
import be.sugoi.wopr.programs.thermo.entities.SimulationSpeed;
import be.sugoi.wopr.programs.thermo.entities.scenario.Scenario;
import be.sugoi.wopr.programs.thermo.entities.scenario.ScenarioMaker;
import be.sugoi.wopr.log.LoggerConfig;
import be.sugoi.wopr.theme.Theme;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class Main extends Game {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 800;
    public static final float WORLD_WIDTH = 1.25f;
    public static final float WORLD_HEIGHT = 1.0f;

    public static final String TTF_NAME = "3270NerdFont-Regular.ttf";
    public static final String FONT_XS = Main.TTF_NAME + "_XS";
    public static final String FONT_M = Main.TTF_NAME + "_M";
    public static final String FONT_XL = Main.TTF_NAME + "_XL";

    // GDX components
    public FitViewport fitViewport;
    public ScreenViewport screenViewport;
    public Stage stage;
    public OrthographicCamera camera;
    public ShapeRenderer shape;
    public SpriteBatch batch;

    // Game components
    public FontManager fm;
    public Settings settings;
    public Theme theme;
    private ScreenManager screenManager;

    // Thermonuclear components
    public Countries countries;
    public Cities cities;
    public ScenarioMaker scenarioMaker;
    private List<Scenario> scenarios;
    private int scenarioIndex;
    public SimulationSpeed simulationSpeed;

    @Override
    public void create() {
        LoggerConfig.configureLogger();
        Gdx.graphics.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        camera = new OrthographicCamera();
        fitViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        screenViewport = new ScreenViewport(camera);
        stage = new Stage(screenViewport);
        shape = new ShapeRenderer();
        batch = new SpriteBatch();

        fm = createFonts();
        settings = loadSettings();
        theme = new Theme(settings.getTheme());

        countries = new Countries();
        countries.load();
        cities = new Cities();
        cities.load();
        scenarioMaker = new ScenarioMaker(cities, countries);

        screenManager = new ScreenManager(this);
        screenManager.notify(this.getClass().getSimpleName(), "start");
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarioIndex(int scenarioIndex) {
        if (scenarioIndex < 0 || scenarioIndex >= scenarios.size()) {
            throw new ArrayIndexOutOfBoundsException("Invalid scenarioIndex: " + scenarioIndex);
        }
        this.scenarioIndex = scenarioIndex;
    }

    public Scenario getScenario() {
        return scenarios.get(scenarioIndex);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Theme changeTheme(String name) {
        settings.setTheme(name);
        theme = new Theme(settings.getTheme());
        return theme;
    }

    private FontManager createFonts() {
        var data = Map.of(
            TTF_NAME, Map.of(
                "XS", 15,
                "M", 20,
                "XL", 40
            )
        );
        var fm = new FontManager("fonts");
        data.forEach((ttf, sizes) -> {
            fm.addTrueTypeFont(ttf);
            sizes.forEach((suffix, size) ->
                fm.newFont(ttf, ttf + "_" + suffix, size, 1.0f)
            );
        });
        return fm;
    }

    private Settings loadSettings() {
        var settings = new Settings();
        try {
            settings.load();
        } catch (FileNotFoundException e) {
            System.err.println("Settings file not found: " + e);
            // Will use default settings
        }
        return settings;
    }

    public ScreenManager sm() {
        return screenManager;
    }

    @Override
    public void dispose() {
        super.dispose();
        fm.dispose();
    }
}
