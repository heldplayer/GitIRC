
package me.heldplayer.irc.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerOutputStream extends ByteArrayOutputStream {

    private Logger log;
    private Level logLevel;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public LoggerOutputStream(Logger log, Level logLevel) {
        this.log = log;
        this.logLevel = logLevel;
    }

    @Override
    public void flush() throws IOException {
        synchronized (LoggerOutputStream.class) {
            super.flush();
            String line = this.toString();
            if (line != null) {
                line = line.replaceAll(LINE_SEPARATOR, "").replaceAll("\t", "    ");
                if (!line.isEmpty()) {
                    log.log(this.logLevel, line);
                }
            }
            super.reset();
        }
    }

}
