package be.sugoi.wopr.terminal;

import com.badlogic.gdx.Screen;

abstract public class BaseScreen implements Screen {
    protected String name() {
        return this.getClass().getSimpleName();
    }
}
