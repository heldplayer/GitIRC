
package me.heldplayer.irc.git.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;

import me.heldplayer.irc.git.GitPlugin;
import me.heldplayer.irc.git.RequestSource;

public abstract class WebResponse {

    private ByteArrayOutputStream headerBytes;
    protected DataOutputStream header;
    private ByteArrayOutputStream bodyBytes;
    protected DataOutputStream body;

    public WebResponse() throws IOException {
        this.headerBytes = new ByteArrayOutputStream();
        this.header = new DataOutputStream(this.headerBytes);
        this.bodyBytes = new ByteArrayOutputStream();
        this.body = new DataOutputStream(this.bodyBytes);
    }

    public abstract WebResponse writeResponse(RequestSource source) throws IOException;

    public void flush(RequestSource source, DataOutputStream stream) throws IOException {
        ByteArrayInputStream header = null;
        ByteArrayInputStream body = null;
        try {
            byte[] bodyBytes = this.bodyBytes.toByteArray();

            this.header.writeBytes("Content-Length: " + bodyBytes.length + "\r\n");
            this.header.writeBytes("\r\n");
            header = new ByteArrayInputStream(this.headerBytes.toByteArray());
            while (header.available() > 0) {
                int bits = header.read();
                stream.write(bits);
            }

            if (source == null || source.method.hasBody) {
                body = new ByteArrayInputStream(bodyBytes);
                while (body.available() > 0) {
                    int bits = body.read();
                    stream.write(bits);
                }
            }
        }
        catch (SocketException ex) {
            GitPlugin.getLog().log(Level.WARNING, "Tried displaying page to a client, but the client closed the connection!", ex);
        }
        catch (IOException ex) {
            GitPlugin.getLog().log(Level.WARNING, "Tried displaying page to a client, but an error occoured", ex);
        }
        finally {
            try {
                if (header != null) {
                    header.close();
                }
            }
            catch (IOException ex) {}
            try {
                this.header.close();
            }
            catch (IOException ex) {}
            try {
                this.headerBytes.close();
            }
            catch (IOException ex) {}
            try {
                if (body != null) {
                    body.close();
                }
            }
            catch (IOException ex) {}
            try {
                this.body.close();
            }
            catch (IOException ex) {}
            try {
                this.bodyBytes.close();
            }
            catch (IOException ex) {}
        }
    }

}
