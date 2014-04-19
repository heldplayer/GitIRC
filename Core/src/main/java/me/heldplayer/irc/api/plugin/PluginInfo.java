
package me.heldplayer.irc.api.plugin;

import me.heldplayer.util.json.JSONObject;

public class PluginInfo {

    public final String name;
    public final String mainClass;

    public PluginInfo(JSONObject object) {
        this.name = object.getString("name");
        if (this.name == null) {
            throw new PluginException("'name' must be set in plugin info");
        }
        this.mainClass = object.getString("main");
        if (this.mainClass == null) {
            throw new PluginException("'main' must be set in plugin info");
        }
    }

}
