package be.sugoi.wopr;

import be.sugoi.wopr.programs.thermo.screens.*;
import be.sugoi.wopr.terminal.LoginScreen;
import be.sugoi.wopr.terminal.ProgramScreen;
import be.sugoi.wopr.terminal.SettingsScreen;
import be.sugoi.wopr.terminal.ThemeScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// Transition from one screen to another.
///
/// This manager takes care of remembering the current screen
/// and the previous ones so that the user can go back.
public class ScreenManager {
    private static final Logger logger = Logger.getLogger(ScreenManager.class.getName());
    private final Main g;
    private final Deque<Class<? extends Screen>> aria = new ArrayDeque<>();
    private Screen currentScreen;

    public ScreenManager(Main g) {
        this.g = g;
    }

    /// Notify of a screen transition event.
    ///
    /// Example: `screenManager.notify(this.class.getSimpleName(), "done")`
    ///
    /// @param origin The screen's simple class name
    /// @param event  An event, e.g. `done` or `back`
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public void notify(String origin, String event) {
        logger.info(String.format("notification from [%s] with event [%s]", origin, event));

        if (ThemeScreen.class.getSimpleName().equals(origin)) {
            if (event.equals("back")) {
                goBack();
                return;
            } else if (event.equals("theme")) {
                return; // self
            } else {
                throw invalidEvent(origin, event);
            }
        } else if (SettingsScreen.class.getSimpleName().equals(origin)) {
            if (event.equals("back")) {
                goBack();
                return;
            } else if (event.equals("settings")) {
                return; // self
            } else {
                throw invalidEvent(origin, event);
            }
        } else {
            // System-wide events
            switch (event) {
                case "theme" -> {
                    gotoScreen(ThemeScreen.class);
                    return;
                }
                case "settings" -> {
                    gotoScreen(SettingsScreen.class);
                    return;
                }
                case "exit" -> {
                    Gdx.app.exit();
                    return;
                }
            }
        }

        // Simple Go Back
        var defaultGoBackScreens = Stream.of(
            ProgramScreen.class, ScenarioScreen.class, ScenarioDetailsScreen.class,
            SpeedScreen.class, WarScreen.class, OutcomeScreen.class
        ).map(Class::getSimpleName).toList();
        if ("back".equals(event) && defaultGoBackScreens.contains(origin)) {
            goBack();
            return;
        }

        // Default "done"
        var defaultDoneScreens = Map.of(
            // origin, destination
            LoginScreen.class, ProgramScreen.class,
            ScenarioScreen.class, ScenarioDetailsScreen.class,
            ScenarioDetailsScreen.class, SpeedScreen.class,
            SpeedScreen.class, WarScreen.class,
            WarScreen.class, OutcomeScreen.class,
            OutcomeScreen.class, ScenarioScreen.class
        ).entrySet().stream().collect(Collectors.toMap(
            entry -> entry.getKey().getSimpleName(),
            Map.Entry::getValue
        ));
        if ("done".equals(event) && defaultDoneScreens.containsKey(origin)) {
            gotoScreen(defaultDoneScreens.get(origin));
            return;
        }

        if (Main.class.getSimpleName().equals(origin)) {
            switch (event) {
                case "start" -> {
                    if (!g.settings.isSkipLogingScreen()) {
                        gotoScreen(LoginScreen.class);
                    } else {
                        gotoScreen(ProgramScreen.class);
                    }
                }
                default -> throw invalidEvent(origin, event);
            }
            return;
        }

        if (ProgramScreen.class.getSimpleName().equals(origin)) {
            switch (event) {
                case "war" -> gotoScreen(ScenarioScreen.class);
                case "back" -> Gdx.app.exit();
                default -> throw invalidEvent(origin, event);
            }
            return;
        }

        throw invalidEvent(origin, event);
    }

    private IllegalArgumentException invalidEvent(String origin, String event) {
        return new IllegalArgumentException(String.format("Invalid event for %s: %s", origin, event));
    }

    private void goBack() {
        if (aria.size() < 2) {
            logger.warning("Cannot go back");
        }
        aria.removeLast();  // self
        var previous = aria.removeLast();
        logger.info("Go back " + previous);
        gotoScreen(previous);
    }

    private void gotoScreen(Class<? extends Screen> screenClass) {
        logger.info("Go to " + screenClass);
        aria.add(screenClass);
        _gotoScreen(screenClass);
    }

    private void _gotoScreen(Class<? extends Screen> screenClass) {
        try {
            Screen screenInstance = screenClass.getDeclaredConstructor(Main.class).newInstance(g);
            if (currentScreen != null) {
                currentScreen.dispose();
            }
            currentScreen = screenInstance;
            g.setScreen(screenInstance);
        } catch (
            InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }
}
