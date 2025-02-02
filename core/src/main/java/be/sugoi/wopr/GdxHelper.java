package be.sugoi.wopr;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class GdxHelper {
    private GdxHelper() {
        // Not meant to be instantiated
    }

    public static float[] pointsToScreenFloatArray(List<Vector2> points) {
        var arr = new float[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            var point = points.get(i);
            arr[i * 2] = point.x;
            arr[i * 2 + 1] = point.y;
        }
        assert arr.length >= 4 : "Polygon must contain at least 2 points, got " + points;
        return arr;
    }
}
