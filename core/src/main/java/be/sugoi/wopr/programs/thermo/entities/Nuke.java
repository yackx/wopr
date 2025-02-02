package be.sugoi.wopr.programs.thermo.entities;

import be.sugoi.wopr.Trajectory;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.badlogic.gdx.math.MathUtils.random;

/// A nuclear missile.
public class Nuke implements Updateable {
    private static final float SPEED_ADJUST = 0.2f;
    private static final float MAX_LAUNCH_DELAY = 60.0f;

    private final @NotNull LaunchSite origin;
    private final @NotNull City destination;
    private float launchDelay;
    private List<Vector2> trajectory;
    private int leg = 0;
    private boolean launched = false;
    private boolean detonated = false;
    private float flyTime = 0;

    public Nuke(@NotNull LaunchSite origin, @NotNull City destination) {
        this.origin = origin;
        this.destination = destination;
        this.launchDelay = random.nextFloat(MAX_LAUNCH_DELAY);
        computeTrajectory();
    }

    private void computeTrajectory() {
        var distance = Trajectory.haversine(origin.coord(), destination.coord());
        var numberOfPoints = (int) (distance * 200);
        trajectory = Trajectory.generateEquidistantPoints(
            origin.coord(), destination.coord(), numberOfPoints
        );
        trajectory.add(destination.coord());
    }

    @Override
    public float update(float delta) {
        if (!launched || detonated) {
            return 0;
        }
        launchDelay = Math.max(0, launchDelay - delta);
        if (launchDelay > 0) {
            return 0;
        }
        flyTime += delta;
        setLeg((int) (flyTime * SPEED_ADJUST));
        return flyTime;
    }

    public void detonate() {
        destination.hit();
        detonated = true;
    }

    public boolean isDetonated() {
        return detonated;
    }

    private void setLeg(int leg) {
        this.leg = Math.clamp(leg, 0, trajectory.size() - 1);
    }

    public int getLeg() {
        return leg;
    }

    public boolean isLaunched() {
        return launched;
    }

    public void launch() {
        launched = true;
    }

    public boolean hasReachedDestination() {
        return leg == trajectory.size() - 1;
    }

    public @NotNull City destination() {
        return destination;
    }

    public @NotNull Vector2 position() {
        return trajectory.get(leg);
    }

    public @NotNull List<Vector2> trajectory() {
        return trajectory;
    }
}
