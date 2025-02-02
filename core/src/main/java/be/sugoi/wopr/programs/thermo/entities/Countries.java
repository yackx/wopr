package be.sugoi.wopr.programs.thermo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

public class Countries {
    private List<Country> countries;

    public void load() {
        JSONObject earth = loadJSON();
        countries = extractCountries(earth);
    }

    private JSONObject loadJSON() {
        var handle = Gdx.files.internal("custom.geo.json");
        // var handle = Gdx.files.internal("WB_countries_Admin0_lowres.geojson");
        var text = handle.readString();
        return new JSONObject(text);
    }

    private void extractPolygon(JSONArray jsonArray, List<Polygon> acc) {
        try {
            var points = IntStream.range(0, jsonArray.length()).mapToObj(idx -> {
                var jsonPoint = jsonArray.getJSONArray(idx);
                return new Vector2(jsonPoint.getFloat(0), jsonPoint.getFloat(1));
            }).toList();
            acc.add(Polygon.of(points));
        } catch (NumberFormatException | JSONException e) {
            for (int i = 0; i < jsonArray.length(); i++) {
                extractPolygon(jsonArray.getJSONArray(i), acc);
            }
        }
    }

    private List<Country> extractCountries(JSONObject jsonObject) {
        countries = new ArrayList<>();
        var features = jsonObject.getJSONArray("features");
        for (int i = 0; i < features.length(); i++) {
            var feature = features.getJSONObject(i);
            Vector2 labelPosition;
            JSONObject properties = feature.getJSONObject("properties");
            var labelX = properties.getFloat("label_x");
            var labelY = properties.getFloat("label_y");
            labelPosition = new Vector2(labelX, labelY);
            var countryName = properties.getString("name_en");
            var isoA2 = properties.getString("iso_a2_eh");
            var population = properties.getInt("pop_est");
            var geometry = feature.getJSONObject("geometry");
            var geometryType = geometry.getString("type");
            var coordinates = geometry.getJSONArray("coordinates");
            List<Polygon> polygons = new ArrayList<>();
            switch (geometryType) {
                case "Polygon", "MultiPolygon" -> extractPolygon(coordinates, polygons);
                default -> throw new IllegalArgumentException("Unknown geometry type: " + geometryType);
            }
            var country = new Country(countryName, isoA2, population, polygons, labelPosition);
            countries.add(country);
        }
        return countries;
    }

    public Country findByName(String name) throws NoSuchElementException {
        return countries.stream().filter(c -> name.equals(c.name())).findFirst().orElseThrow();
    }

    public Country findByCode(String code) throws NoSuchElementException {
        return countries.stream().filter(c -> code.equals(c.countryCode())).findFirst().orElseThrow();
    }

    public int size() {
        return countries.size();
    }

    public List<Country> getCountries() {
        return countries;
    }
}
