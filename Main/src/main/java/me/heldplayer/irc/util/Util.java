
package me.heldplayer.irc.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.heldplayer.irc.api.configuration.Configuration;

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

    public static String createGitIO(String address) {
        try {
            String param = URLEncoder.encode(address, "UTF-8");
            URL url = new URL("http://git.io/create");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes("url=" + param);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.readLine();
            in.close();

            return response;
        }
        catch (Throwable e) {
            throw new RuntimeException("Failed creating git.io link", e);
        }
    }

}
