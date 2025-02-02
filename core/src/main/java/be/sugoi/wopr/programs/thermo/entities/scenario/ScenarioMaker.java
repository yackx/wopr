package be.sugoi.wopr.programs.thermo.entities.scenario;

import be.sugoi.wopr.Trajectory;
import be.sugoi.wopr.programs.thermo.entities.*;
import be.sugoi.wopr.programs.thermo.screens.MapView;
import be.sugoi.wopr.utils.AssertionCheck;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.badlogic.gdx.math.MathUtils.random;


public class ScenarioMaker {
    private static final int SUBMARINE_LAUNCH_CAPACITY = 20;

    private final @NotNull Cities cities;
    private final @NotNull Countries countries;

    public ScenarioMaker(@NotNull Cities cities, @NotNull Countries countries) {
        this.cities = cities;
        this.countries = countries;
    }

    public List<Scenario> load() {
        var text = loadYaml();
        var json = parse(text);
        return extractScenariosFromJSON(json);
    }

    private String loadYaml() {
        var handle = Gdx.files.internal("scenarios.json");
        return handle.readString();
    }

    private JSONObject parse(String text) {
        return new JSONObject(text);
    }

    /**
     * Parse launch sites from JSON.
     * </p>
     * Launch sites are stored separately, so that they can be reused across scenarios.
     *
     * @param jsonLaunchSites JSON root containing launch sites
     * @return List of sites
     */
    private List<LaunchSite> parseLaunchSites(JSONArray jsonLaunchSites) {
        List<LaunchSite> launchSites = new ArrayList<>();
        for (int countryIndex = 0; countryIndex < jsonLaunchSites.length(); countryIndex++) {
            var jsonCountrySites = jsonLaunchSites.getJSONObject(countryIndex);
            var countryCode = jsonCountrySites.getString("country");
            var jsonSites = jsonCountrySites.getJSONArray("sites");
            for (int siteIndex = 0; siteIndex < jsonSites.length(); siteIndex++) {
                var jsonSite = jsonSites.getJSONObject(siteIndex);
                var jsonCoord = jsonSite.getJSONObject("coord");
                var siteTypeStr = jsonSite.getString("type");
                var siteType = LaunchSite.Type.fromString(siteTypeStr);
                launchSites.add(new LaunchSite(
                    jsonSite.getString("name"),
                    countryCode,
                    new Vector2(jsonCoord.getFloat("lon"), jsonCoord.getFloat("lat")),
                    siteType
                ));
            }
        }
        return launchSites;
    }

    private List<Scenario> extractScenariosFromJSON(JSONObject root) {
        var allLaunchSites = parseLaunchSites(root.getJSONArray("launchSites"));
        var jsonScenarios = root.getJSONArray("scenarios");
        return IntStream.range(0, jsonScenarios.length())
            .mapToObj(jsonScenarios::getJSONObject)
            .map(it -> extractScenarioFromJSON(it, allLaunchSites))
            .toList();
    }

    private Scenario extractScenarioFromJSON(JSONObject jsonScenario, List<LaunchSite> allLaunchSites) {
        var jsonParties = jsonScenario.getJSONArray("parties");
        var partiesCount = jsonParties.length();
        if (partiesCount < 2) {
            throw new IllegalArgumentException("Insufficient number of parties: " + partiesCount);
        }
        var partiesData = IntStream.range(0, partiesCount)
            .mapToObj(i -> parseParty(jsonParties.getJSONObject(i)))
            .toList();
        var parties = partiesData.stream().map(data -> {
                var enemiesCountryCodes = partiesData.stream()
                    .filter(it -> data != it)
                    .filter(it -> data.enemiesNames().contains(it.name()))
                    .flatMap(it -> it.countryCodes().stream())
                    .toList();
                assert !enemiesCountryCodes.isEmpty();
                var launchSites = allLaunchSites.stream()
                    .filter(ls -> data.countryCodes().contains(ls.countryCode()))
                    .toList();
                return createParty(data, launchSites, enemiesCountryCodes);
            })
            .toList();

        var firstStrike = parties.get(jsonScenario.optIntegerObject(
            "firstStrike", random.nextInt(parties.size())
        ));
        var title = jsonScenario.getString("short");
        var description = jsonScenario.getString("description");

        var initialMapViewStr = jsonScenario.getString("initialMapView");
        var initialMapView = MapView.getByName(initialMapViewStr);

        return new Scenario(title, description, parties, firstStrike, initialMapView);
    }

    private Party createParty(ScenarioPartyData data, List<LaunchSite> launchSites, List<String> enemiesCountryCodes) {
        // Retrieve country members
        var countries = data.countryCodes().stream()
            .map(this.countries::findByCode)
            .toList();

        // Retrieve this party's cities
        var cities = this.cities.getCities().stream()
            .filter(city -> data.countryCodes().contains(city.countryCode()))
            .toList();

        // Retrieve enemy cities
        var enemyCities = this.cities.getCities().stream()
            .filter(city -> enemiesCountryCodes.contains(city.countryCode()))
            .sorted(Comparator.comparingInt(City::population).reversed())
            .collect(Collectors.toCollection(ArrayList::new));

        // If there are more warheads than enemy cities,
        // hit the cities multiple times
        if (enemyCities.size() < data.warheads()) {
            int i = data.warheads() / enemyCities.size();
            //noinspection CollectionAddedToSelf
            IntStream.range(0, i).forEach(it -> enemyCities.addAll(enemyCities));
        }

        // Set the launch sites capacities.
        // Submarines and fixed
        // Facilities should be balanced.
        launchSites.stream()
            .filter(it -> it.getType() == LaunchSite.Type.SUBMARINE)
            .forEach(it -> it.setRemainingCapacity(SUBMARINE_LAUNCH_CAPACITY));
        var facilitiesCount = launchSites.stream()
            .filter(it -> it.getType() == LaunchSite.Type.FACILITY)
            .count();
        int facilityCapacity = (int) (data.warheads() / Math.max(1, facilitiesCount - 1));
        launchSites.stream()
            .filter(it -> it.getType() == LaunchSite.Type.FACILITY)
            .forEach(it -> it.setRemainingCapacity(facilityCapacity));

        // Create nukes.
        // Enemy cities are targeted once based on population
        // (multiple hits should be more effective).
        // The launch site is selected based on distance.
        if (AssertionCheck.areAssertionsEnabled()) {
            var totalCapacity = launchSites.stream().mapToInt(LaunchSite::remainingCapacity).sum();
            assert totalCapacity >= data.warheads()
                : String.format(
                "insufficient launch sites for [%s]: %d vs %d",
                data.name(), totalCapacity, data.warheads());
        }
        var nukes = new ArrayList<>(IntStream.range(0, data.warheads())
            .mapToObj(i -> {
                var target = enemyCities.removeFirst();
                var site = launchSites.stream()
                    .filter(Predicate.not(LaunchSite::isEmpty))
                    .min(Comparator.comparingDouble(it -> Trajectory.haversine(it.coord(), target.coord())))
                    .orElseThrow(() -> new IllegalStateException("no launch site available"));
                site.decrementCapacity();
                return new Nuke(site, target);
            })
            .toList());

        var name = data.name();
        var shortName = data.shortName();

        return new Party(name, shortName, countries, cities, launchSites, nukes);
    }

    private ScenarioPartyData parseParty(JSONObject jsonParty) {
        var jsonCountryCodes = jsonParty.getJSONArray("countryCodes");
        var countryCodes = IntStream.range(0, jsonCountryCodes.length())
            .mapToObj(jsonCountryCodes::getString)
            .toList();

        var jsonEnemies = jsonParty.getJSONArray("enemies");
        var enemiesNames = IntStream.range(0, jsonEnemies.length())
            .mapToObj(jsonEnemies::getString)
            .toList();

        var name = jsonParty.getString("name");
        var shortName = jsonParty.optString("shortName", name);
        var warheads = jsonParty.getInt("warheads");


        var jsonLaunchSites = jsonParty.optJSONArray("launchSites");
        List<String> launchSitesNames;
        if (jsonLaunchSites != null) {
            launchSitesNames = IntStream
                .range(0, jsonLaunchSites.length())
                .mapToObj(jsonLaunchSites::getJSONObject)
                .map(jsonLaunchSite -> jsonLaunchSite.getString("name"))
                .toList();
        } else {
            launchSitesNames = List.of();
        }

        return new ScenarioPartyData(name, shortName, countryCodes, launchSitesNames, warheads, enemiesNames);
    }
}
