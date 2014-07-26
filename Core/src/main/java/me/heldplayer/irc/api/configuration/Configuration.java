package me.heldplayer.irc.api.configuration;

import java.io.*;
import java.util.TreeMap;

public class Configuration {

    private File file;
    private TreeMap<String, String> defaults = new TreeMap<String, String>();
    private TreeMap<String, String> entries;

    public Configuration(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("file", e);
            }
        }
        this.file = file;
    }

    public void setDefault(String key, String value) {
        this.defaults.put(key, value);
    }

    public void setDefault(String key, int value) {
        this.defaults.put(key, "" + value);
    }

    public void setDefault(String key, boolean value) {
        this.defaults.put(key, "" + value);
    }

    public String getString(String key) {
        if (this.entries.containsKey(key)) {
            return this.entries.get(key);
        }
        if (this.defaults.containsKey(key)) {
            return this.defaults.get(key);
        }
        return null;
    }

    public int getInt(String key) {
        if (this.entries.containsKey(key)) {
            String entry = this.entries.get(key);
            try {
                return Integer.parseInt(entry);
            } catch (NumberFormatException e) {
                throw new ConfigurationException(e);
            }
        }
        if (this.defaults.containsKey(key)) {
            String entry = this.defaults.get(key);
            try {
                return Integer.parseInt(entry);
            } catch (NumberFormatException e) {
                throw new ConfigurationException(e);
            }
        }
        return 0;
    }

    public boolean getBoolean(String key) {
        if (this.entries.containsKey(key)) {
            String entry = this.entries.get(key);
            return Boolean.parseBoolean(entry);
        }
        if (this.defaults.containsKey(key)) {
            String entry = this.defaults.get(key);
            return Boolean.parseBoolean(entry);
        }
        return false;
    }

    public void load() {
        BufferedReader reader = null;

        this.entries = new TreeMap<String, String>();

        try {
            reader = new BufferedReader(new FileReader(this.file));

            String line = "";

            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!line.startsWith("#")) {
                    if (line.indexOf('#') > 0) {
                        line = line.substring(0, line.indexOf('#')).trim();
                    }
                    String[] split = line.split("=", 2);
                    if (split.length >= 2) {
                        this.entries.put(split[0].trim(), split[1].trim());
                    } else {
                        throw new ConfigurationException("Configuration requires 'key=value' on line " + lineNumber);
                    }
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException("Failed reading configuration", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        boolean defaultMissing = false;

        for (String key : this.defaults.keySet()) {
            if (!this.entries.containsKey(key)) {
                defaultMissing = true;
            }
        }

        if (defaultMissing) {
            TreeMap<String, String> merged = new TreeMap<String, String>();

            for (String key : this.defaults.keySet()) {
                if (!this.entries.containsKey(key)) {
                    merged.put(key, this.defaults.get(key) + " #GENERATED DEFAULT");
                }
            }
            merged.putAll(this.entries);

            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new FileWriter(this.file));

                for (String key : merged.keySet()) {
                    writer.write(key + "=" + merged.get(key));
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new ConfigurationException("Failed writing configuration", e);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

}
