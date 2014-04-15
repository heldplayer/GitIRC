
package me.heldplayer.web.server.internal.security.require;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public class AllowFrom extends IpRangeRule {

    public AllowFrom(JSONObject object) {
        super(object);
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        return this.matches(source);
    }

}
