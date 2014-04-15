
package me.heldplayer.web.server.internal.security.rules;

import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;

public class DenyFrom extends IpRangeRule {

    public DenyFrom(JSONObject object) {
        super(object);
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        return !this.matches(source);
    }

}
