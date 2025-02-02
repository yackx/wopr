package be.sugoi.wopr.utils;

public class AssertionCheck {
    private static final boolean assertionsEnabled;

    static {
        boolean assertsEnabled = false;
        //noinspection AssertWithSideEffects
        assert assertsEnabled = true; // Intentional side effect
        //noinspection ConstantValue
        assertionsEnabled = assertsEnabled;
    }

    public static boolean areAssertionsEnabled() {
        return assertionsEnabled;
    }
}
