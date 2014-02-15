
package me.heldplayer.web.server.internal.security.require;

import java.net.InetAddress;
import java.net.UnknownHostException;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public class RequireIp implements Rule {

    private InetAddress address;

    public RequireIp(JSONObject object) {
        String address = object.getString("value");
        try {
            this.address = InetAddress.getByName(address);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        return source.address.equals(this.address);
    }

}
