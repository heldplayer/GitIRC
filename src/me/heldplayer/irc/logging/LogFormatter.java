
package me.heldplayer.irc.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private SimpleDateFormat dateFormat;

    public LogFormatter() {
        this.dateFormat = new SimpleDateFormat("WW/dd/yyyy HH:mm:ss");
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();

        builder.append("[").append(this.dateFormat.format(new Date(record.getMillis()))).append("] ");
        builder.append("[").append(record.getLoggerName()).append("] ");
        builder.append("[").append(record.getLevel().getName()).append("] ");
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

}
