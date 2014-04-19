
package me.heldplayer.web.server.internal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.web.server.RequestSource;
import me.heldplayer.web.server.GitPlugin;
import me.heldplayer.web.server.event.HttpRequestEvent;
import me.heldplayer.web.server.internal.ErrorResponse.ErrorType;

public class RunnableHttpResponse implements Runnable {

    private final Socket socket;
    private DataOutputStream out;
    private BufferedReader in;
    private long timeout = 0;
    protected boolean finished = false;

    public RunnableHttpResponse(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        RequestSource source = null;
        String location = null;

        main:
        {
            try {
                this.out = new DataOutputStream(this.socket.getOutputStream());
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                ArrayList<String> input = new ArrayList<String>();

                while (!this.in.ready()) {
                    this.timeout++;

                    if (this.timeout > 30000L) {
                        break;
                    }
                    try {
                        Thread.sleep(1L);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }

                while (this.in.ready()) {
                    String line = this.in.readLine();
                    if (line.isEmpty()) {
                        break;
                    }
                    input.add(line);
                }

                if (input.size() <= 0) {
                    new ErrorResponse(ErrorType.BadRequest).writeResponse(source).flush(source, this.out);

                    break main;
                }

                String[] split = input.remove(0).split(" ");
                String method = split[0];

                location = split[1];
                if (location.startsWith("/")) {
                    location = location.substring(1);
                }
                String[] splitLocation = location.split("/");
                int chunks = 0;
                for (String chunk : splitLocation) {
                    if (!chunk.isEmpty()) {
                        chunks++;
                    }
                }
                if (splitLocation.length != chunks) {
                    String[] temp = new String[chunks];
                    int j = 0;
                    for (String chunk : splitLocation) {
                        if (!chunk.isEmpty()) {
                            temp[j] = chunk;
                            j++;
                        }
                    }
                    splitLocation = temp;
                }

                location = URLDecoder.decode(split[1], "UTF-8");
                while (!(location.indexOf("\\.\\.") < 0)) {
                    location = location.replaceAll("\\.\\.", ".");
                }
                if (location.endsWith("/")) {
                    location = location.concat("index.htm");
                }

                String version = split[2];

                TreeMap<String, String> headers = new TreeMap<String, String>();

                for (String line : input) {
                    String[] headerSplit = line.split(": ", 2);
                    if (headerSplit.length < 2) {
                        headers.put(headerSplit[0], "");
                    }
                    else {
                        headers.put(headerSplit[0], headerSplit[1]);
                    }
                }

                source = new RequestSource(this.socket.getInetAddress(), RequestMethod.fromString(method), headers, location);

                if (headers.containsKey("Content-Length")) {
                    Integer length = Integer.parseInt(headers.get("Content-Length"));

                    char[] data = new char[length];
                    int i = 0;
                    long lastRead = System.currentTimeMillis();
                    while (i < length) {
                        if (this.in.ready()) {
                            data[i] = (char) this.in.read();
                            i++;
                            lastRead = System.currentTimeMillis();
                        }
                        if (lastRead + 10000L < System.currentTimeMillis()) {
                            throw new IOException("Connection timed out");
                        }
                    }
                    source.body = new String(data);
                }

                if (source.method == RequestMethod.NULL) {
                    new ErrorResponse(ErrorType.NotImplemented).writeResponse(source).flush(source, this.out);

                    break main;
                }

                if (!version.split("/")[1].equals("1.0") && !version.split("/")[1].equals("1.1")) {
                    new ErrorResponse(ErrorType.HTTPVersionNotSupported).writeResponse(source).flush(source, this.out);

                    break main;
                }

                File root = new File("web");
                File file = new File(root, location).getAbsoluteFile();

                if (!RunnableWebserver.instance.accessManager.canView(splitLocation, source)) {
                    GitPlugin.getLog().info(String.format("Denied access to view '%s' from '%s'", source.path, source.address.getHostAddress()));

                    for (Entry<String, String> entry : headers.entrySet()) {
                        GitPlugin.getLog().info(String.format("%s: %s", entry.getKey(), entry.getValue()));
                    }

                    new ErrorResponse(ErrorType.Forbidden).writeResponse(source).flush(source, this.out);

                    break main;
                }

                HttpRequestEvent event = new HttpRequestEvent(source);
                BotAPI.eventBus.postEvent(event);

                if (event.response != null) {
                    event.response.writeResponse(source).flush(source, this.out);

                    break main;
                }
                else if (event.error != null) {
                    new ErrorResponse(event.error).writeResponse(source).flush(source, this.out);

                    break main;
                }

                if (file.isDirectory()) {
                    new ErrorResponse(ErrorType.Forbidden).writeResponse(source).flush(source, this.out);

                    break main;
                }
                else if (file.exists()) {
                    new FileResponse(file).writeResponse(source).flush(source, this.out);

                    break main;
                }
                else {
                    new ErrorResponse(ErrorType.NotFound).writeResponse(source).flush(source, this.out);

                    break main;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (RuntimeException ex) {
                if (!ex.getMessage().equalsIgnoreCase("break")) {
                    GitPlugin.getLog().log(Level.SEVERE, "Exception while responding to web client", ex);
                    GitPlugin.getLog().log(Level.SEVERE, "Request: " + location);
                }

                try {
                    new ErrorResponse(ErrorType.InternalServerError).writeResponse(source).flush(source, this.out);
                }
                catch (IOException e) {}
            }
            finally {
                try {
                    this.out.close();
                    this.in.close();
                    this.socket.close();
                    this.finished = true;
                    return;
                }
                catch (IOException e) {}
            }
        }

        try {
            this.out.close();
            this.in.close();
            this.socket.close();
            this.finished = true;
            return;
        }
        catch (IOException e) {}

        this.finished = true;
    }

}
