
package me.heldplayer.web.server.internal.security.require;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public class DenyAll implements Rule {

    public DenyAll(JSONObject object) {}

    @Override
    public boolean checkAccess(RequestSource source) {
        return false;
    }

}
