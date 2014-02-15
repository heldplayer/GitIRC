
package me.heldplayer.web.server.internal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.TreeMap;
import java.util.logging.Level;

import me.heldplayer.web.server.RequestSource;
import me.heldplayer.web.server.WebServerEntryPoint;
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

                TreeMap<Integer, String> input = new TreeMap<Integer, String>();
                Integer i = 0;

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
                    input.put(i++, line);
                }

                if (input.size() <= 0) {
                    new ErrorResponse(ErrorType.BadRequest).writeResponse(source).flush(source, this.out);

                    break main;
                }

                String[] split = input.get(0).split(" ");
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

                source = new RequestSource(this.socket.getInetAddress(), RequestMethod.fromString(method), location);

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
                    WebServerEntryPoint.log.info(String.format("Denied access to view '%s' from '%s'", source.path, source.address.getHostAddress()));

                    new ErrorResponse(ErrorType.Forbidden).writeResponse(source).flush(source, this.out);

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
                    WebServerEntryPoint.log.log(Level.SEVERE, "Exception while responding to web client", ex);
                    WebServerEntryPoint.log.log(Level.SEVERE, "Request: " + location);
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
