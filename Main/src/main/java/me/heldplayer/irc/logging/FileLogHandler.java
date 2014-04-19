
package me.heldplayer.irc.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class FileLogHandler extends Handler {

    private PrintStream out;

    public FileLogHandler(String filename, boolean append) throws IOException, SecurityException {
        this.out = new PrintStream(new FileOutputStream(new File(filename), append));
    }

    @Override
    public void publish(LogRecord record) {
        synchronized (this.out) {
            this.out.println(this.getFormatter().format(record));
        }
    }

    @Override
    public void close() throws SecurityException {
        synchronized (this.out) {
            this.out.close();
        }
    }

    @Override
    public void flush() {
        synchronized (this.out) {
            this.out.flush();
        }
    }

}
