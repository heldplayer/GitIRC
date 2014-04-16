
package me.heldplayer.web.server.internal.security.rules;

import java.net.InetAddress;
import java.net.UnknownHostException;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public abstract class IpRangeRule implements Rule {

    private String value;
    private byte[] bytes;
    private boolean[] wildcards;
    private InetAddress address;

    public IpRangeRule(JSONObject object) {
        this.value = object.getString("value");

        try {
            this.address = InetAddress.getByName(this.value);
            this.bytes = new byte[0];
            this.wildcards = new boolean[0];
        }
        catch (UnknownHostException e) {
            String[] split = this.value.split("\\.");
            this.bytes = new byte[split.length];
            this.wildcards = new boolean[split.length];

            for (int i = 0; i < this.bytes.length; i++) {
                if (split[i].equals("*")) {
                    this.wildcards[i] = true;
                }
                else {
                    this.bytes[i] = (byte) Integer.parseInt(split[i]);
                }
            }
        }

    }

    public boolean matches(RequestSource source) {
        if (this.address != null) {
            return this.address.equals(source.address);
        }

        byte[] bytes = source.address.getAddress();

        for (int i = 0; i < bytes.length && i < this.bytes.length; i++) {
            if (this.wildcards[i] || this.bytes[i] == bytes[i]) {
                continue;
            }
            else {
                return false;
            }
        }

        return true;
    }
}
