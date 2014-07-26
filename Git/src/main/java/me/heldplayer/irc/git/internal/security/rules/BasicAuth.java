package me.heldplayer.irc.git.internal.security.rules;

import me.heldplayer.irc.git.RequestSource;
import me.heldplayer.util.json.JSONObject;

import javax.xml.bind.DatatypeConverter;

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
