
package me.heldplayer.irc.git.internal.security.rules;

import java.util.ArrayList;

import me.heldplayer.irc.git.RequestSource;
import me.heldplayer.irc.git.internal.security.AccessManager;
import me.heldplayer.util.json.JSONArray;
import me.heldplayer.util.json.JSONObject;

public class RequireNone implements Rule {

    private ArrayList<Rule> rules;

    public RequireNone(JSONObject object) {
        JSONArray values = object.getArray("value");
        this.rules = new ArrayList<Rule>();
        for (int i = 0; i < values.size(); i++) {
            JSONObject obj = values.getObject(i);
            this.rules.add(AccessManager.createRule(obj));
        }
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        for (Rule rule : this.rules) {
            if (rule.checkAccess(source)) {
                return false;
            }
        }
        return true;
    }

}
