
package me.heldplayer.irc.logging;

import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

public class ConsoleLogHandler extends ConsoleHandler {

    private PrintStream stream;

    public ConsoleLogHandler(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void publish(LogRecord record) {
        stream.println(this.getFormatter().format(record));

        Throwable thrown = record.getThrown();

        if (thrown != null) {
            thrown.printStackTrace();
        }
    }

}
