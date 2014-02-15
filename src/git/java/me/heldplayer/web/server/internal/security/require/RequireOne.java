
package me.heldplayer.web.server.internal.security.require;

import java.util.ArrayList;

import me.heldplayer.util.json.JSONArray;
import me.heldplayer.util.json.JSONObject;
import me.heldplayer.web.server.RequestSource;
import me.heldplayer.web.server.internal.security.AccessManager;

public class RequireOne implements Rule {

    private ArrayList<Rule> rules;

    public RequireOne(JSONObject object) {
        JSONArray values = object.getArray("value");
        this.rules = new ArrayList<Rule>();
        for (int i = 0; i < values.size(); i++) {
            JSONObject obj = values.getObject(i);
            this.rules.add(AccessManager.createRule(obj));
        }
    }

    @Override
    public boolean checkAccess(RequestSource source) {
        for (Rule rule : rules) {
            if (rule.checkAccess(source)) {
                return true;
            }
        }
        return false;
    }

}
