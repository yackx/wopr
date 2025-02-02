package be.sugoi.wopr.terminal;

public class InvalidDirective extends IllegalArgumentException {
    public InvalidDirective(String message) {
        super(message);
    }
}
