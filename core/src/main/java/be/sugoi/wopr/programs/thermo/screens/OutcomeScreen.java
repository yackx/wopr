package be.sugoi.wopr.programs.thermo.screens;

import be.sugoi.wopr.Main;
import be.sugoi.wopr.programs.thermo.entities.City;
import be.sugoi.wopr.programs.thermo.entities.Party;
import be.sugoi.wopr.terminal.InvalidDirective;
import be.sugoi.wopr.terminal.TerminalScreen;
import be.sugoi.wopr.utils.StringUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static be.sugoi.wopr.utils.StringUtils.fill;
import static be.sugoi.wopr.utils.StringUtils.rpad;
import static java.util.stream.Collectors.joining;

public class OutcomeScreen extends TerminalScreen {
    private static final int COL_WIDTH = 12;
    private static final int FIRST_COL_WIDTH = 24;

    private static final float WINNER_LABEL_DELAY = 2.0f;
    private static final float WINNER_VALUE_DELAY = 1.0f;

    public OutcomeScreen(Main g) {
        super(g);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }

    @Override
    protected @NotNull String templatePath() {
        return "screens/terminal/thermo/outcome.txt";
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive, @NotNull String value) throws InvalidDirective {
        throw newInvalidDirective(directive);
    }

    @Override
    protected @NotNull List<Label> renderDirective(@NotNull String directive) throws InvalidDirective {
        return switch (directive) {
            case "STATS_TABLE" -> renderStats();
            case "FATALITIES_TOTAL" -> List.of(renderTotalFatalitiesCount());
            case "WINNER" -> List.of(renderWinner());
            default -> throw newInvalidDirective(directive);
        };
    }

    private int tableWidth() {
        return FIRST_COL_WIDTH + g.getScenario().parties().size() * COL_WIDTH;
    }

    private Label renderTableHorizontalLine() {
        var h = fill(tableWidth(), '─');
        return toLabel(h, Align.center);
    }

    private List<Label> renderStats() {
        List<Label> labels = new ArrayList<>();
        labels.add(renderTableHorizontalLine());
        labels.add(renderParties());
        labels.add(renderBlankLine());
        labels.add(renderReceivedSubHeader());
        labels.add(renderFatalitiesCount());
        labels.add(renderFatalitiesPercent());
        labels.add(renderDetonationsCount());
        labels.add(renderTableHorizontalLine());
        return labels;
    }

    private Label renderParties() {
        var str = g.getScenario().parties().stream()
            .map(Party::shortName)
            .map(name -> rpad(name, COL_WIDTH))
            .collect(joining());
        str = rpad("DAMAGES", FIRST_COL_WIDTH) + str;
        return toLabel(str, Align.center);
    }

    private static String formatToMillions(long number) {
        double millions = number / 1_000_000.0;
        DecimalFormat decimalFormat = new DecimalFormat("#.#M");
        return decimalFormat.format(millions);
    }

    private Label renderReceivedSubHeader() {
        var subHeader = rpad("Received:", FIRST_COL_WIDTH) + fill(g.getScenario().parties().size() * COL_WIDTH);
        return toLabel(subHeader, Align.center);
    }

    private Label renderFatalitiesCount() {
        var str = g.getScenario().parties().stream()
            .map(Party::fatalities)
            .map(OutcomeScreen::formatToMillions)
            .map(it -> rpad(it, COL_WIDTH))
            .collect(joining());
        str = rpad("Fatalities (day 1)", FIRST_COL_WIDTH) + str;
        return toLabel(str, Align.center);
    }

    private Label renderFatalitiesPercent() {
        var str = g.getScenario().parties().stream()
            .map(party -> {
                var killRate = (float) party.fatalities() / party.population();
                return StringUtils.formatToPercentage(killRate);
            })
            .map(it -> rpad(it, COL_WIDTH))
            .collect(joining());
        str = fill(FIRST_COL_WIDTH) + str;
        return toLabel(str, Align.center);
    }

    private Label renderTotalFatalitiesCount() {
        var total = g.getScenario().parties().stream()
            .mapToInt(Party::fatalities)
            .sum();
        var str = "TOTAL: " + StringUtils.formatWithThousandSeparator(total);
        var style = createBaseStyle();
        style.font = g.fm.getFont(Main.FONT_XL);
        style.fontColor = g.theme.color2();
        return toLabel(str, Align.center, style);
    }

    private Label renderDetonationsCount() {
        var str = g.getScenario().parties().stream()
            .map(p -> p.cities().stream()
                .mapToInt(City::hits)
                .sum())
            .map(n -> Integer.toString(n))
            .map(it -> rpad(it, COL_WIDTH))
            .collect(joining());
        str = rpad("Detonations", FIRST_COL_WIDTH) + str;
        return toLabel(str, Align.center);
    }

    private Label renderWinner() {
        String str;
        if (clock < WINNER_LABEL_DELAY) {
            str = " ";
        } else if (clock < WINNER_LABEL_DELAY + WINNER_VALUE_DELAY) {
            str = "WINNER:     ";
        } else {
            str = "WINNER: NONE";
        }
        var style = createBaseStyle();
        style.font = g.fm.getFont(Main.FONT_XL);
        style.fontColor = g.theme.color4();
        return toLabel(str, Align.center, style);
    }

    @Override
    protected void processInput() {
        super.processInput();
        if (kb.isEnterPressed()) {
            kb.reset();
            g.sm().notify(name(), "done");
        }
    }
}
