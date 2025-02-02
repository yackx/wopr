package be.sugoi.wopr.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class KeyboardSingle extends InputAdapter {
    public static final int NO_KEY_RECORDED = -1;

    private int keyCode;

    public KeyboardSingle() {
        reset();
    }

    @Override
    public boolean keyDown(int keycode) {
        this.keyCode = keycode;
        return true;
    }

    public int keyCode() {
        return keyCode;
    }

    public void reset() {
        keyCode = NO_KEY_RECORDED;
    }

    public boolean isEnterPressed() {
        return keyCode == Input.Keys.ENTER || keyCode == Input.Keys.NUMPAD_ENTER;
    }

    public boolean isEscapePressed() {
        return keyCode == Input.Keys.ESCAPE;
    }
}
