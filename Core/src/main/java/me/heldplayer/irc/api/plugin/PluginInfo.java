
package me.heldplayer.irc.api.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.heldplayer.util.json.JSONArray;
import me.heldplayer.util.json.JSONObject;

public class PluginInfo {

    public final String name;
    public final String mainClass;
    public final List<Dependency> dependencies;

    public PluginInfo(JSONObject object) {
        this.name = object.getString("name");
        if (this.name == null) {
            throw new PluginException("'name' must be set in plugin info");
        }
        this.mainClass = object.getString("main");
        if (this.mainClass == null) {
            throw new PluginException("'main' must be set in plugin info");
        }

        JSONArray dependencies = object.getArray("dependencies");
        ArrayList<Dependency> dependencyList = new ArrayList<PluginInfo.Dependency>();

        if (dependencies != null) {
            for (Object obj : dependencies.values) {
                if (!(obj instanceof String)) {
                    throw new PluginException("'dependencies' must be an array of strings");
                }

                String value = (String) obj;
                String[] split = value.split(":", 2);
                if (split.length != 2) {
                    throw new PluginException("dependency must be of format <type>:<id>");
                }

                DependencyOrder order = DependencyOrder.valueOf(split[0].toUpperCase());
                if (order == null) {
                    throw new PluginException("dependency order must be (require_)before/after");
                }

                dependencyList.add(new Dependency(order, split[1]));
            }
        }

        this.dependencies = Collections.unmodifiableList(dependencyList);
    }

    public DependencyOrder getOrder(String plugin) {
        for (Dependency dependency : dependencies) {
            if (dependency.name.equalsIgnoreCase(plugin)) {
                return dependency.order;
            }
        }
        return null;
    }

    public static class Dependency {

        public final DependencyOrder order;
        public final String name;

        public Dependency(DependencyOrder order, String name) {
            this.order = order;
            this.name = name;
        }

    }

    public static enum DependencyOrder {

        REQUIRE_BEFORE(true, 1), BEFORE(false, 1), AFTER(false, -1), REQUIRE_AFTER(true, -1);

        public final boolean require;
        public final int compare;

        private DependencyOrder(boolean require, int compare) {
            this.require = require;
            this.compare = compare;
        }

    }

}
