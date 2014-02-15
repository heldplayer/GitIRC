
package me.heldplayer.web.server;

import java.net.InetAddress;

import me.heldplayer.web.server.internal.RequestMethod;

public class RequestSource {

    public final InetAddress address;
    public String path;

    public final RequestMethod method;

    public RequestSource(InetAddress address, RequestMethod method, String path) {
        this.address = address;
        this.method = method;
        this.path = path;
    }

}
