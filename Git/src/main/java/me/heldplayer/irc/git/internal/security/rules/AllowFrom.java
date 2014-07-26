package me.heldplayer.irc.git.internal.security.rules;

import me.heldplayer.irc.git.RequestSource;
import me.heldplayer.util.json.JSONObject;

public class AllowFrom extends IpRangeRule {

    public AllowFrom(JSONObject object) {
        super(object);
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        return this.matches(source);
    }

}
