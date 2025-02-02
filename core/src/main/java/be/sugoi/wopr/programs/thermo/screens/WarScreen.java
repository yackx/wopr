package be.sugoi.wopr.programs.thermo.screens;

import be.sugoi.wopr.GdxFontHelper;
import be.sugoi.wopr.GdxHelper;
import be.sugoi.wopr.Main;
import be.sugoi.wopr.Projection;
import be.sugoi.wopr.input.KeyboardSingle;
import be.sugoi.wopr.programs.thermo.entities.*;
import be.sugoi.wopr.programs.thermo.entities.scenario.Scenario;
import be.sugoi.wopr.terminal.BaseScreen;
import be.sugoi.wopr.theme.Theme;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/// Thermonuclear war screen.
public class WarScreen extends BaseScreen {

    private enum Phase {
        WAITING_TO_START, NUKING_THE_WORLD, TERMINATED
    }

    private static final float CITY_LARGE_DOT_SCALE = 1.5f;
    private static final float NUKE_SIZE = 0.005f;
    private static final float LAUNCH_SITE_SIDE_SIZE = 0.015f;
    private static final float LAUNCH_SITE_HEIGHT_SIZE = (float) (Math.sqrt(3) / 2 * LAUNCH_SITE_SIDE_SIZE);
    private static final float TRAJECTORY_DOT_SIZE = 0.001f;
    private static final float DETONATION_MIN_LIGHT_INTENSITY = 0.25f;
    private static final float DETONATION_DIAMETER = 0.0075f;
    private static final int DETONATION_CIRCLE_SEGMENTS = 12;

    private final Main g;
    private final KeyboardSingle kb;
    private final Sound launchSound;
    private final @NotNull FPSLogger fps = new FPSLogger();
    private float clock = 0f;
    private boolean paused = false;
    private @NotNull Phase phase = Phase.WAITING_TO_START;
    private @NotNull MapView mapView = MapView.EUROPE;
    private Map<Party, Color> nukeColors;
    private boolean showCityLabels = true;
    private boolean showTrajectories = true;

    // Parameters for projection

    // Map offset (range 0.0-1.0)
    private Vector2 offset;
    // Map zoom. Nominal value is 1.0f * world height to display the whole map
    private float zoom;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();

    // Precomputed screen projections (expensive)
    // Logical models can be converted to screen coordinates and cached.
    // Recompute on zoom or offset resize.

    private List<float[]> polylines;
    private final @NotNull Map<Nuke, List<Vector2>> screenTrajectories = new HashMap<>();

    public WarScreen(Main game) {
        this.g = game;
        kb = new KeyboardSingle();
        launchSound = Gdx.audio.newSound(Gdx.files.internal("sounds/launch/strider.mp3"));
    }

    @Override
    public void show() {
        setView(g.getScenario().initialView());
        showCityLabels = g.settings.isShowCityLabels();
        Gdx.input.setInputProcessor(kb);
    }

    private void setView(MapView mapView) {
        this.mapView = mapView;
        offset = mapView.offset;
        zoom = Main.WORLD_HEIGHT * mapView.zoomFactor;
        precompute();
    }

    private void precompute() {
        polylines = buildCountriesPolylines();
        screenTrajectories.putAll(buildScreenTrajectories(g.getScenario().allNukes()));
        nukeColors = buildNukesColorMap();
    }

    /**
     * Build a map of color per party.
     * <p/>
     * Each party gets a different color (for its nukes and its launch sites)
     * if there are at most 5 parties and the color option is set.
     * @return Map of color per party
     */
    private Map<Party, Color> buildNukesColorMap() {
        var scenario = g.getScenario();
        var colors = g.settings.isColorNukesPerParty() && scenario.parties().size() <= 5 ?
            List.of(
                g.theme.danger(), g.theme.color3(), g.theme.color4(), g.theme.color5(), g.theme.color6()
            ) :
            Collections.nCopies(scenario.parties().size(), g.theme.danger());
        return IntStream.range(0, scenario.parties().size())
            .boxed()
            .collect(toMap(
                i -> scenario.parties().get(i),
                colors::get
            ));
    }

    private List<float[]> buildCountriesPolylines() {
        return g.countries.getCountries().stream()
            .map(Country::polygons)
            .flatMap(List::stream)
            .flatMap(polygon -> splitPolygon(polygon).stream())
            .map(cappedPolygon -> Projection.polygonToScreenProjection(cappedPolygon, offset, zoom))
            .map(projectedPolygon -> GdxHelper.pointsToScreenFloatArray(projectedPolygon.getPoints()))
            .toList();
    }

    private List<Polygon> splitPolygon(Polygon polygon) {
        // shape.polyline accepts at most 2000 floats.
        // Some polygons have more values than that -> split
        final int limit = 2000;
        List<Polygon> polygonList = new ArrayList<>();
        if (polygon.count() <= limit) {
            polygonList.add(polygon);
        } else {
            for (int idx = 0; idx < polygon.count(); idx += limit) {
                polygonList.add(polygon.subShape(idx, Math.min(idx + limit, polygon.count())));
            }
        }
        return polygonList;
    }

    private Map<Nuke, List<Vector2>> buildScreenTrajectories(List<Nuke> nukes) {
        return nukes.stream().collect(toMap(
            Function.identity(),
            nuke -> nuke.trajectory().stream()
                .map(p -> Projection.coordinatesToScreenProjection(p, offset, zoom))
                .collect(toList())
        ));
    }

    @Override
    public void render(float delta) {
        fps.log();
        input();
        logic(delta);
        draw();
    }

    private void input() {
        if (kb.keyCode() == KeyboardSingle.NO_KEY_RECORDED) {
            return;
        }

        // Escape
        if (kb.isEscapePressed()) {
            g.sm().notify(name(), "back");
            kb.reset();
            return;
        }

        // ENTER
        if (phase == Phase.TERMINATED && kb.isEnterPressed()) {
            g.sm().notify(name(), "done");
            kb.reset();
            return;
        }

        // Pause
        if (kb.keyCode() == Input.Keys.P && phase == Phase.NUKING_THE_WORLD) {
            paused = !paused;
            kb.reset();
            return;
        }

        // Faster or slower
        if (kb.keyCode() == Input.Keys.F6) {
            g.simulationSpeed = g.simulationSpeed.faster();
            kb.reset();
            return;
        }
        if (kb.keyCode() == Input.Keys.F5) {
            g.simulationSpeed = g.simulationSpeed.slower();
            kb.reset();
            return;
        }

        // City labels
        if (kb.keyCode() == Input.Keys.C) {
            showCityLabels = !showCityLabels;
            kb.reset();
            return;
        }

        // Trajectories
        if (kb.keyCode() == Input.Keys.T) {
            showTrajectories = !showTrajectories;
            kb.reset();
            return;
        }

        // Change view
        var viewForKey = Map.of(
            Input.Keys.NUM_1, MapView.EUROPE,
            Input.Keys.NUMPAD_1, MapView.EUROPE,
            Input.Keys.NUM_2, MapView.RUSSIA,
            Input.Keys.NUMPAD_2, MapView.RUSSIA,
            Input.Keys.NUM_3, MapView.CHINA,
            Input.Keys.NUMPAD_3, MapView.CHINA,
            Input.Keys.NUM_9, MapView.NORTH_AMERICA,
            Input.Keys.NUMPAD_9, MapView.NORTH_AMERICA,
            Input.Keys.NUM_0, MapView.WORLD,
            Input.Keys.NUMPAD_0, MapView.WORLD
        );
        viewForKey.keySet().stream()
            .filter(k -> kb.keyCode() == k)
            .findFirst()
            .ifPresentOrElse(
                integer -> setView(viewForKey.get(integer)),
                () -> {
                    if (phase == Phase.WAITING_TO_START) {
                        launch();
                    }
                }
        );

        // Ignore
        kb.reset();
    }

    private void launch() {
        phase = Phase.NUKING_THE_WORLD;
        if (g.settings.isSoundEffects()) {
            launchSound.play();
        }
    }

    private void logic(float delta) {
        if (paused) {
            return;
        }

        if (phase == Phase.WAITING_TO_START) {
            return;
        }

        // Update wall clock with speed factor
        final float speedDelta = delta * g.simulationSpeed.speedFactor();
        clock += speedDelta;

        // Detect terminal state
        var scenario = g.getScenario();
        if (scenario.allNukes().stream().allMatch(Nuke::hasReachedDestination)) {
            phase = Phase.TERMINATED;
        }

        // Cruise nukes
        scenario.parties().stream()
            .filter(p -> clock > Scenario.RETALIATION_DELAY || p == scenario.firstStrike())
            .flatMap(p -> p.nukes().stream().filter(Predicate.not(Nuke::isDetonated)))
            .forEach(nuke -> {
                nuke.launch();
                nuke.update(speedDelta);
            });

        // Detect terminal nukes
        var terminalNukes = scenario.allNukes().stream()
            .filter(Nuke::hasReachedDestination)
            .filter(Predicate.not(Nuke::isDetonated))
            .toList();
        // Remove them from screen vectors
        terminalNukes.forEach(screenTrajectories::remove);
        // Detonate
        terminalNukes.forEach(scenario::detonate);

        // Update detonations
        scenario.allDetonations().forEach(detonation -> detonation.update(delta));
    }

    private void draw() {
        ScreenUtils.clear(g.theme.background());
        g.fitViewport.apply();
        g.batch.setProjectionMatrix(g.camera.combined);
        drawWorld();
        drawCountryNames();
        drawCityLabels();
        drawCities();
        drawDetonations();
        drawLaunchSites();
        drawNukes();
        drawStats();
        drawStartInstructions();
        drawPause();
        drawTerminated();
    }

    private void drawStartInstructions() {
        if (phase != Phase.WAITING_TO_START) {
            return;
        }
        drawAction("Press any key to launch");
    }

    private void drawPause() {
        if (!paused) {
            return;
        }
        drawAction("Paused");
    }

    private void drawTerminated() {
        if (phase != Phase.TERMINATED) {
            return;
        }
        drawAction("Terminated - Press ENTER");
    }

    private void drawAction(String text) {
        var font = g.fm.getFont(Main.FONT_XL);
        var msgDimension = GdxFontHelper.getDimensions(font, text);
        var charDimension = GdxFontHelper.getDimensions(font, " ");

        var x = GdxFontHelper.centerX(font, text, Main.WORLD_WIDTH);
        var y = 1.5f * msgDimension.y;

        g.shape.begin(ShapeRenderer.ShapeType.Filled);
        g.shape.setColor(g.theme.danger());
        g.shape.rect(x - charDimension.x / 2, 0, msgDimension.x + charDimension.x, msgDimension.y * 2f);
        g.shape.end();

        g.batch.begin();
        font.setColor(g.theme.color3());
        font.draw(g.batch, text.toUpperCase(), x, y);
        g.batch.end();
    }

    private void drawStats() {
        if (phase == Phase.WAITING_TO_START) {
            return;
        }

        g.shape.begin(ShapeRenderer.ShapeType.Filled);
        g.shape.setColor(g.theme.background());
        g.shape.rect(0.0f, 0.9f, 0.3f, 0.1f);
        g.shape.end();

        g.batch.begin();
        var fontXL = g.fm.getFont(Main.FONT_XL);
        fontXL.setColor(g.theme.danger());
        String formattedFatalities = numberFormat.format(g.getScenario().fatalities());
        fontXL.draw(g.batch, formattedFatalities, 0.0f, 0.99f);
        var fontM = g.fm.getFont(Main.FONT_M);
        fontM.setColor(g.theme.danger());
        fontM.draw(g.batch, "Detonations : " + g.getScenario().allDetonations().size(), 0.0f, 0.95f);
        fontM.draw(g.batch, "Nukes       : " + g.getScenario().allAirborneNukes().size(), 0.0f, 0.93f);
        g.batch.end();
    }

    private void drawWorld() {
        g.shape.begin(ShapeRenderer.ShapeType.Line);
        g.shape.setColor(g.theme.color1());
        polylines.forEach(polyline -> g.shape.polyline(polyline));
        g.shape.end();
    }

    private void drawDetonations() {
        g.shape.begin(ShapeRenderer.ShapeType.Filled);
        g.getScenario().allDetonations().forEach(detonation -> {
            var screenPos = Projection.coordinatesToScreenProjection(detonation.city().coord(), offset, zoom);
            float i = Math.max(DETONATION_MIN_LIGHT_INTENSITY, detonation.lightIntensity());
            g.shape.setColor(new Color(i, i, i, 1.0f));
            g.shape.circle(
                screenPos.x,
                screenPos.y,
                detonation.diameter() * DETONATION_DIAMETER * mapView.detonationFactor,
                DETONATION_CIRCLE_SEGMENTS
            );
        });
        g.shape.end();
    }

    private void drawCountryNames() {
        g.batch.begin();
        var font = g.fm.getFont(Main.FONT_XS);
        font.setColor(g.theme.color1(Theme.Modifier.MUTE));
        for (var country : g.countries.getCountries()) {
            if (country.population() < mapView.countryPopulationThresholdLarge) {
                continue;
            }
            var position = country.labelPosition();
            var projected = Projection.coordinatesToScreenProjection(position, offset, zoom);
            font.draw(g.batch, country.countryCode(), projected.x, projected.y);
        }
        g.batch.end();
    }

    private void drawCityLabels() {
        if (!showCityLabels) {
            return;
        }
        g.batch.begin();
        var font = g.fm.getFont(Main.FONT_XS);
        font.setColor(g.theme.accentColor());
        for (var city : g.cities.getCities()) {
            if (city.population() < mapView.cityPopulationThresholdLarge) {
                continue;
            }
            var coord = city.coord();
            var projected = Projection.coordinatesToScreenProjection(coord, offset, zoom);
            font.draw(g.batch, city.name(), projected.x, projected.y);
        }
        g.batch.end();
    }

    private void drawCities() {
        g.shape.begin(ShapeRenderer.ShapeType.Filled);
        g.shape.setColor(g.theme.color2());
        var cities = g.cities.getCities().stream()
            .filter(city -> city.population() >= mapView.cityPopulationThresholdMinimum)
            .toList();
        for (var city : cities) {
            var coord = city.coord();
            var projected = Projection.coordinatesToScreenProjection(coord, offset, zoom);
            float factor;
            if (city.population() > mapView.cityPopulationThresholdLarge) {
                factor = CITY_LARGE_DOT_SCALE;
            } else {
                factor = 1.0f;
            }
            g.shape.rect(projected.x, projected.y, mapView.citySquareSize * factor, mapView.citySquareSize * factor);
        }
        g.shape.end();
    }

    private void drawLaunchSites() {
        g.shape.begin(ShapeRenderer.ShapeType.Filled);
        g.getScenario().parties().forEach(p -> {
                g.shape.setColor(nukeColors.get(p));
                p.launchSites().stream()
                    .map(LaunchSite::coord)
                    .map(position ->
                        Projection.coordinatesToScreenProjection(position, offset, zoom))
                    .forEach(v -> g.shape.triangle(
                        v.x, v.y + LAUNCH_SITE_HEIGHT_SIZE / 3,
                        v.x - LAUNCH_SITE_SIDE_SIZE / 2, v.y - LAUNCH_SITE_HEIGHT_SIZE / 3,
                        v.x + LAUNCH_SITE_SIDE_SIZE / 2, v.y - LAUNCH_SITE_HEIGHT_SIZE / 3
                    ));
            });
        g.shape.end();
    }

    private void drawNukes() {
        if (phase != Phase.NUKING_THE_WORLD) {
            return;
        }
        g.shape.begin(ShapeRenderer.ShapeType.Line);
        g.getScenario().airborneNukesPerParty().forEach((party, nukes) -> {
            g.shape.setColor(nukeColors.get(party));
            nukes.forEach(nuke -> {
                drawHead(nuke);
                drawTrajectory(nuke);
            });
        });
        g.shape.end();
    }

    private void drawHead(Nuke nuke) {
        var position = Projection.coordinatesToScreenProjection(nuke.position(), offset, zoom);
        var half = NUKE_SIZE / 2;
        g.shape.rect(
            position.x - half, position.y - half,
            half, half,
            NUKE_SIZE, NUKE_SIZE, 1f, 1f, 45f
        );
    }

    private void drawTrajectory(Nuke nuke) {
        if (!showTrajectories) {
            return;
        }
        var nukeScreenTrajectory = screenTrajectories.get(nuke);
        IntStream.range(Math.max(0, nuke.getLeg() - 4), nuke.getLeg())
            .mapToObj(nukeScreenTrajectory::get)
            .forEach(screenPoint ->
                g.shape.rect(screenPoint.x, screenPoint.y, TRAJECTORY_DOT_SIZE, TRAJECTORY_DOT_SIZE)
            );
    }

    @Override
    public void resize(int width, int height) {
        g.fitViewport.update(width, height, true);
        g.batch.setProjectionMatrix(g.camera.combined);
        g.shape.setProjectionMatrix(g.camera.combined);
        g.fm.setScaleForAll(g.fitViewport.getWorldHeight() / Gdx.graphics.getHeight());
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        launchSound.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
