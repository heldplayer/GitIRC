
package me.heldplayer.irc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.configuration.Configuration;

public final class Util {

    public static final Logger log = Logger.getLogger("IRCBot");

    public static BufferedReader openResource(String path) {
        URL url = Configuration.class.getResource(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        catch (IOException e) {
            Util.log.log(Level.SEVERE, "Failed opening resource '" + path + "'", e);
        }

        return reader;
    }

    public static BufferedReader openFile(String filename) {
        File file = new File(filename);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
        }
        catch (IOException e) {
            Util.log.log(Level.SEVERE, "Failed opening file '" + filename + "'", e);
        }

        return reader;
    }

}
