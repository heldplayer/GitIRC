
package me.heldplayer.irc.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConsoleLogFormatter extends Formatter {

    private SimpleDateFormat dateFormat;

    public ConsoleLogFormatter() {
        //this.dateFormat = new SimpleDateFormat("WW/dd/yyyy HH:mm:ss");
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();

        builder.append("[").append(this.dateFormat.format(new Date(record.getMillis()))).append("] ");
        builder.append(ensureEqualLength(record.getLoggerName(), 8)).append(" ");
        builder.append('[').append(ensureEqualLength(record.getLevel().getName(), 7)).append("] ");
        //builder.append("[").append(record.getLoggerName()).append("] ");
        if (record.getParameters() != null) {
            builder.append(String.format(record.getMessage(), record.getParameters()));
        }
        else {
            builder.append(record.getMessage());
        }
        if (record.getThrown() != null) {
            builder.append(LoggerOutputStream.LINE_SEPARATOR);

            StringWriter sw = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(sw));
            builder.append(sw.toString());
        }

        return builder.toString().trim();
    }

    private static String ensureEqualLength(String string, int length) {
        StringBuilder result = new StringBuilder();
        result.append(string);
        while (result.length() < length) {
            result.append(' ');
        }
        boolean trimmed = false;
        while (result.length() > length) {
            result.deleteCharAt(result.length() - 1);
            trimmed = true;
        }
        if (trimmed) {
            result.deleteCharAt(result.length() - 1);
            result.append(".");
        }
        return result.toString();
    }
}
