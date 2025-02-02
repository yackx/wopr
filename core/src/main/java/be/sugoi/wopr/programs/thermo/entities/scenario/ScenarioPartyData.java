package be.sugoi.wopr.programs.thermo.entities.scenario;

import be.sugoi.wopr.programs.thermo.entities.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// Scenario party - data
///
/// @implNote
/// Each [Party] needs some info of the other (target cities) to be created.
/// This record holds the party data as read from file.
public record ScenarioPartyData(
    @NotNull String name,
    @NotNull String shortName,
    @NotNull List<String> countryCodes,
    @NotNull List<String> launchSitesNames,
    int warheads,
    @NotNull List<String> enemiesNames
) {
}
