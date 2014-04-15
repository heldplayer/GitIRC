
package me.heldplayer.web.server.internal.security.require;

import javax.xml.bind.DatatypeConverter;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public class BasicAuth implements Rule {

    private String hashed;

    public BasicAuth(JSONObject object) {
        String plain = object.getString("value");
        this.hashed = DatatypeConverter.printBase64Binary(plain.getBytes());
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        if (source.headers.containsKey("Authorization")) {
            return source.headers.get("Authorization").equals("Basic " + this.hashed);
        }
        return false;
    }

}
