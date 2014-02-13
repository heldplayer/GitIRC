
package me.heldplayer.web.server.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;

public abstract class WebResponse {

    private ByteArrayOutputStream baos;
    protected DataOutputStream out;

    public WebResponse() throws IOException {
        this.baos = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.baos);
    }

    public abstract WebResponse writeResponse(RequestFlags flags) throws IOException;

    public void flush(DataOutputStream stream) throws IOException {
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(this.baos.toByteArray());
            while (in.available() > 0) {
                int bits = in.read();
                stream.write(bits);
            }
        }
        catch (SocketException ex) {
            RunnableWebserver.log.log(Level.WARNING, "Tried displaying page to a client, but the client closed the connection!", ex);
        }
        catch (IOException ex) {
            RunnableWebserver.log.log(Level.WARNING, "Tried displaying page to a client, but an error occoured", ex);
        }
        finally {
            try {
                this.out.close();
            }
            catch (IOException ex) {}
            try {
                this.baos.close();
            }
            catch (IOException ex) {}
            try {
                in.close();
            }
            catch (IOException ex) {}
        }
    }

}
