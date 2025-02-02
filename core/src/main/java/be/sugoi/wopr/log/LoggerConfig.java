package be.sugoi.wopr.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LoggerConfig {

    public static void configureLogger() {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.ALL);

        // Remove default handlers
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            logger.removeHandler(handler);
        }

        // Add custom StreamHandler to log to stdout
        StreamHandler streamHandler = new StreamHandler(System.out, new Iso8601Formatter());
        streamHandler.setLevel(Level.ALL);
        logger.addHandler(streamHandler);
    }
}
