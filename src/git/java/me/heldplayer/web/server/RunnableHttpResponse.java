
package me.heldplayer.web.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.TreeMap;
import java.util.logging.Level;

import me.heldplayer.web.server.ErrorResponse.ErrorType;

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
        RequestFlags flags = new RequestFlags();
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
                    new ErrorResponse(ErrorType.BadRequest).writeResponse(flags).flush(this.out);

                    break main;
                }

                String[] split = input.get(0).split(" ");
                String method = split[0];
                location = split[1];
                String version = split[2];

                flags.method = RequestFlags.Method.fromString(method);

                if (flags.method == RequestFlags.Method.NULL) {
                    new ErrorResponse(ErrorType.NotImplemented).writeResponse(flags).flush(this.out);

                    break main;
                }

                if (!version.split("/")[1].equalsIgnoreCase("1.0") && !version.split("/")[1].equalsIgnoreCase("1.1")) {
                    new ErrorResponse(ErrorType.HTTPVersionNotSupported).writeResponse(flags).flush(this.out);

                    break main;
                }

                location = URLDecoder.decode(location, "UTF-8");

                while (!(location.indexOf("\\.\\.") < 0)) {
                    location = location.replaceAll("\\.\\.", ".");
                }

                if (location.endsWith("/")) {
                    location = location.concat("index.htm");
                }

                File root = new File("web");
                File file = new File(root, location).getAbsoluteFile();

                if (file.isDirectory()) {
                    new ErrorResponse(ErrorType.Forbidden).writeResponse(flags).flush(this.out);

                    break main;
                }
                else if (file.exists()) {
                    new FileResponse(file).writeResponse(flags).flush(this.out);

                    break main;
                }
                else {
                    new ErrorResponse(ErrorType.NotFound).writeResponse(flags).flush(this.out);

                    break main;
                }
            }
            catch (WebGuiException ex) {
                try {
                    ex.response.writeResponse(flags).flush(this.out);
                }
                catch (IOException e) {
                    RunnableWebserver.log.log(Level.SEVERE, "Exception while responding to web client", ex);
                    RunnableWebserver.log.log(Level.SEVERE, "Request: " + location);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (RuntimeException ex) {
                if (!ex.getMessage().equalsIgnoreCase("break")) {
                    RunnableWebserver.log.log(Level.SEVERE, "Exception while responding to web client", ex);
                    RunnableWebserver.log.log(Level.SEVERE, "Request: " + location);
                }

                try {
                    new ErrorResponse(ErrorType.InternalServerError).writeResponse(flags).flush(this.out);
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
