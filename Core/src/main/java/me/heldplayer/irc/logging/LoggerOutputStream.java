
package me.heldplayer.irc.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.api.sandbox.SandboxBlacklist;

@SandboxBlacklist
public class LoggerOutputStream extends ByteArrayOutputStream {

    private Logger log;
    private Level logLevel;
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

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
                line = line.replaceAll(LoggerOutputStream.LINE_SEPARATOR, "");
                if (!line.isEmpty()) {
                    this.log.log(this.logLevel, line);
                }
            }
            super.reset();
        }
    }

}
