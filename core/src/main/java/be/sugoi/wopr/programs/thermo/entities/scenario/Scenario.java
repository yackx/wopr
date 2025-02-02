package be.sugoi.wopr.programs.thermo.entities.scenario;

import be.sugoi.wopr.programs.thermo.entities.Detonation;
import be.sugoi.wopr.programs.thermo.entities.Nuke;
import be.sugoi.wopr.programs.thermo.entities.Party;
import be.sugoi.wopr.programs.thermo.screens.MapView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/// A scenario in the simulation.
public class Scenario {
    public static final float RETALIATION_DELAY = 60f;

    private final @NotNull String name;
    private final @NotNull String description;
    private final @NotNull List<Party> parties;
    private final @NotNull Party firstStrike;
    private final @NotNull MapView initialMapView;

    public Scenario(
        @NotNull String name,
        @NotNull String description,
        @NotNull List<Party> parties,
        @NotNull Party firstStrike,
        @NotNull MapView initialMapView)
    {
        this.name = name;
        this.description = description;
        this.parties = parties;
        this.firstStrike = firstStrike;
        this.initialMapView = initialMapView;
    }

    /// Detonate a nuke.
    ///
    /// @param nuke Nuke to detonate.
    /// Must have reached its destination.
    /// @return The corresponding detonation
    @SuppressWarnings("UnusedReturnValue")
    public @NotNull Detonation detonate(Nuke nuke) {
        if (!nuke.hasReachedDestination()) {
            throw new IllegalStateException("Cannot detonate a non terminal nuke: " + nuke);
        }
        Party target = parties.stream()
            .filter(p -> p.cities().contains(nuke.destination()))
            .findFirst()
            .orElseThrow();
        nuke.detonate();
        var detonation = new Detonation(nuke.destination(), 1.0f);
        target.detonations().add(detonation);
        return detonation;
    }

    public @NotNull List<Nuke> allNukes() {
        return parties.stream().flatMap(party -> party.nukes().stream()).toList();
    }

    public @NotNull List<Nuke> allAirborneNukes() {
        return parties.stream()
            .flatMap(party -> party.nukes().stream()
                .filter(n -> !n.hasReachedDestination() && n.isLaunched()))
            .toList();
    }

    public @NotNull Map<Party, List<Nuke>> airborneNukesPerParty() {
        return parties.stream().collect(Collectors.toMap(
            Function.identity(),
            party -> party.nukes().stream()
                .filter(n -> !n.hasReachedDestination() && n.isLaunched())
                .toList()
        ));
    }

    public @NotNull List<Detonation> allDetonations() {
        return parties.stream().flatMap(party -> party.detonations().stream()).toList();
    }

    public int fatalities() {
        return parties.stream().mapToInt(Party::fatalities).sum();
    }

    public @NotNull List<Party> parties() {
        return parties;
    }

    public @NotNull Party firstStrike() {
        return firstStrike;
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String description() {
        return description;
    }

    public @NotNull MapView initialView() {
        return initialMapView;
    }
}
