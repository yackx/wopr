package be.sugoi.wopr.programs.thermo.entities;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class Party {
    private final @NotNull String name;
    private final @NotNull String shortName;
    private final @NotNull List<Country> countries;
    private final @NotNull List<City> cities;
    private final @NotNull List<LaunchSite> launchSites;
    private final @NotNull List<Nuke> nukes;
    private final @NotNull List<Detonation> detonations;

    public Party(
        @NotNull String name,
        @NotNull String shortName,
        @NotNull List<Country> countries,
        @NotNull List<City> cities,
        @NotNull List<LaunchSite> launchSites,
        @NotNull List<Nuke> nukes
    ) {
        this.name = name;
        this.shortName = shortName;
        assert cities.stream().allMatch(city ->
            countries.stream()
                .map(Country::countryCode)
                .toList()
                .contains(city.countryCode())) :
        "city vs country code mismatch";

        this.countries = countries;
        this.cities = cities;
        this.launchSites = launchSites;
        this.nukes = nukes;
        this.detonations = new ArrayList<>();
    }

    public int fatalities() {
        return cities.stream().mapToInt(City::fatalities).sum();
    }

    public int population() {
        return cities.stream().mapToInt(City::population).sum();
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull String shortName() {
        return shortName;
    }

    public @NotNull List<Country> countries() {
        return countries;
    }

    public @NotNull List<City> cities() {
        return cities;
    }

    public @NotNull List<LaunchSite> launchSites() {
        return launchSites;
    }

    public @NotNull List<Nuke> nukes() {
        return nukes;
    }

    public @NotNull List<Detonation> detonations() {
        return detonations;
    }

    @Override
    public String toString() {
        return name;
    }
}
