package be.sugoi.wopr.programs.thermo.entities;

public enum SimulationSpeed {
    REAL_TIME(1f), FAST(25f), FASTER(100f), LIGHTNING_FAST(400f);

    private final float speedFactor;

    SimulationSpeed(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    public float speedFactor() {
        return speedFactor;
    }

    public SimulationSpeed faster() {
        var ord = Math.min(this.ordinal() + 1, SimulationSpeed.values().length-1);
        return SimulationSpeed.values()[ord];
    }

    public SimulationSpeed slower() {
        var ord = Math.max(this.ordinal() - 1, 0);
        return SimulationSpeed.values()[ord];
    }
}
