package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Cities {
    private @NotNull List<City> cities = new ArrayList<>();

    public void load() {
        var handle = Gdx.files.internal("worldcities.csv");
        var text = handle.readString();
        cities = Stream.of(text.split("\n"))
            .skip(1)
            .map(this::parseCity)
            .toList();
    }

    private City parseCity(String line) {
        var parts = Arrays.stream(line.split(",")).map(s -> s.replaceAll("\"", "")).toList();
        var asciiName = parts.get(1);
        var coord = new Vector2(Float.parseFloat(parts.get(3)), Float.parseFloat(parts.get(2)));
        var countryCode = parts.get(5);

        int population;
        try {
            population = Integer.parseInt(parts.get(9));
        } catch (NumberFormatException e) {
            try {
                population = Integer.parseInt(parts.get(10));
            } catch (NumberFormatException ee) {
                population = 0;
            }
        }

        return new City(asciiName, coord, countryCode, population);
    }

    public @NotNull List<City> getCities() {
        return cities;
    }

}
