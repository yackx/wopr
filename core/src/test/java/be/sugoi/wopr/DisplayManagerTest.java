package be.sugoi.wopr;

import be.sugoi.wopr.dm.DisplayManager;
import be.sugoi.wopr.dm.DisplayModeReplicant;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplayManagerTest {

    private Graphics graphics;

    @Before
    public void setUp() {
        graphics = mock(Graphics.class);
        Gdx.graphics = graphics;
    }

    @Test
    public void testListDisplayModesFilters() {
        when(graphics.getDisplayModes()).thenReturn(new Graphics.DisplayMode[]{
            new DisplayModeReplicant(1920, 1200, 30, 24),
            new DisplayModeReplicant(1920, 1200, 60, 24),
            new DisplayModeReplicant(1920, 1200, 120, 24),
        });

        var modes = DisplayManager.listDisplayModes();

        assertEquals(1, modes.size());
        assertEquals(120, modes.getFirst().refreshRate);
    }

    @Test
    public void testListDisplayModesFiltersMultiple() {
        when(graphics.getDisplayModes()).thenReturn(new Graphics.DisplayMode[]{
            new DisplayModeReplicant(1920, 1200, 60, 24),
            new DisplayModeReplicant(1920, 1200, 120, 24),
            new DisplayModeReplicant(3840, 2400, 60, 24),
            new DisplayModeReplicant(3840, 2400, 120, 24),
        });

        var modes = DisplayManager.listDisplayModes();

        assertEquals(2, modes.size());
        assertTrue(modes.contains(new DisplayModeReplicant(1920, 1200, 120, 24)));
        assertTrue(modes.contains(new DisplayModeReplicant(3840, 2400, 120, 24)));
    }

    @Test
    public void testGetBestDisplayMode() {
        var available = Stream.of(
            new DisplayModeReplicant(1920, 1200, 60, 24),
            new DisplayModeReplicant(1920, 1200, 120, 24),
            new DisplayModeReplicant(3840, 2400, 120, 24),
            new DisplayModeReplicant(3840, 2400, 60, 24),
            new DisplayModeReplicant(3500, 2500, 120, 24)
        )
            .map(it -> (Graphics.DisplayMode) it)
            .toList();

        var best = DisplayManager.getBestDisplayMode(available);

        assertEquals(
            new DisplayModeReplicant(3840, 2400, 120, 24),
            DisplayModeReplicant.fromDisplayMode(best)
        );
    }

    @Test
    public void testGetSafestDisplayMode() {
        var available = Stream.of(
            new DisplayModeReplicant(1920, 1200, 60, 24),
            new DisplayModeReplicant(1920, 1200, 120, 24),
            new DisplayModeReplicant(3840, 2400, 120, 24),
            new DisplayModeReplicant(3840, 2400, 60, 24),
            new DisplayModeReplicant(3500, 2500, 120, 24)
        )
            .map(it -> (Graphics.DisplayMode) it)
            .toList();

        var safest = DisplayManager.getSafestDisplayMode(available);

        assertEquals(
            new DisplayModeReplicant(1920, 1200, 120, 24),
            DisplayModeReplicant.fromDisplayMode(safest)
        );
    }
}
