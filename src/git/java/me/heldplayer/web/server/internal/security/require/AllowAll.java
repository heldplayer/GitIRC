
package me.heldplayer.web.server.internal.security.require;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public class AllowAll implements Rule {

    public AllowAll(JSONObject object) {}

    @Override
    public boolean checkAccess(RequestSource source) {
        return true;
    }

}
