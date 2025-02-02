package be.sugoi.wopr.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class Iso8601Formatter extends Formatter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    @Override
    public String format(LogRecord record) {
        String timestamp = dateFormat.format(new Date(record.getMillis()));
        return String.format(
            "%s %s %s: %s%n",
            timestamp, record.getLevel(), record.getLoggerName(), record.getMessage());
    }
}
