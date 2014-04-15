
package me.heldplayer.irc.api.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

public class Configuration {

    private File file;
    private TreeMap<String, String> entries;

    public Configuration(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                throw new IllegalArgumentException("file", e);
            }
        }
        this.file = file;
    }

    public String getString(String key) {
        if (this.entries.containsKey(key)) {
            return this.entries.get(key);
        }
        return null;
    }

    public int getInt(String key) {
        if (this.entries.containsKey(key)) {
            String entry = this.entries.get(key);
            try {
                return Integer.parseInt(entry);
            }
            catch (NumberFormatException e) {
                throw new ConfigurationException(e);
            }
        }
        return 0;
    }

    public void load() {
        BufferedReader reader = null;

        this.entries = new TreeMap<String, String>();

        try {
            reader = new BufferedReader(new FileReader(this.file));
            if (reader != null) {
                String line = "";

                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        String[] split = line.split("=", 2);
                        if (split.length >= 2) {
                            this.entries.put(split[0].trim(), split[1].trim());
                        }
                        else {
                            throw new ConfigurationException("Invalid configuration entry at line " + lineNumber);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw new ConfigurationException("Failed reading configuration", e);
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException e) {}
        }
    }

}
