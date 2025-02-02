package be.sugoi.wopr.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import java.util.List;

public class KeyboardBuffer extends InputAdapter {
    private StringBuilder buffer;
    private int specialKey;

    public KeyboardBuffer() {
        reset();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (isEnterPressed()) {
            // no further processing
            return false;
        }

        if (keycode == Input.Keys.UNKNOWN) {
            // ignore
            return false;
        }

        // Backspace
        if (keycode == Input.Keys.BACKSPACE && !buffer.isEmpty()) {
             buffer.deleteCharAt(buffer.length()-1);
        }

        // Numpad ENTER equivalence
        if (keycode == Input.Keys.NUMPAD_ENTER) {
            keycode = Input.Keys.ENTER;
        }

        // Special keys
        var special = List.of(Input.Keys.ESCAPE, Input.Keys.ENTER);
        if (special.contains(keycode)) {
            specialKey = keycode;
            return true;
        }
        if (keycode >= Input.Keys.F1 && keycode <= Input.Keys.F12) {
            specialKey = keycode;
            return true;
        }

        char c = convertKeycodeToChar(keycode);
        if (c != 0) {
            buffer.append(c);
            return true;
        }

        return false;
    }

    private char convertKeycodeToChar(int keycode) {
        if (keycode >= Input.Keys.NUM_0 && keycode <= Input.Keys.NUM_9) {
            return (char) ('0' + keycode - Input.Keys.NUM_0);
        } else if (keycode >= Input.Keys.NUMPAD_0 && keycode <= Input.Keys.NUMPAD_9) {
            return (char) ('0' + keycode - Input.Keys.NUMPAD_0);
        } else if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            return (char) ('a' + keycode - Input.Keys.A);
        }
        return 0;
    }

    public void reset() {
        buffer = new StringBuilder();
        specialKey = 0;
    }

    public String buffer() {
        return buffer.toString();
    }

    public boolean isEnterPressed() {
        return specialKey == Input.Keys.ENTER;
    }

    public boolean isEscapePressed() {
        return specialKey == Input.Keys.ESCAPE;
    }

    public int getSpecialKey() {
        return specialKey;
    }
}
