
package me.heldplayer.irc.git.internal.security;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.TreeMap;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.git.GitPlugin;
import me.heldplayer.irc.git.RequestSource;
import me.heldplayer.irc.git.event.AccessManagerInitEvent;
import me.heldplayer.irc.git.internal.security.rules.Rule;
import me.heldplayer.util.json.JSONObject;

public class AccessManager {

    private static TreeMap<String, Class<? extends Rule>> ruleTypes = new TreeMap<String, Class<? extends Rule>>();

    public static void registerRule(String type, Class<? extends Rule> clazz) {
        AccessManager.ruleTypes.put(type, clazz);
    }

    public static void cleanupRules() {
        AccessManager.ruleTypes.clear();
    }

    public static Rule createRule(JSONObject object) {
        String type = object.getString("type");
        Class<? extends Rule> clazz = AccessManager.ruleTypes.get(type);

        if (clazz == null) {
            throw new RuntimeException("Unknown AccessRule of type '" + type + "'");
        }

        try {
            Constructor<? extends Rule> constructor = clazz.getConstructor(JSONObject.class);
            return constructor.newInstance(object);
        }
        catch (Throwable e) {
            throw new RuntimeException("Failed creating AccessRule of type '" + type + "'", e);
        }
    }

    private TreeMap<String, AccessConfigRule> configRules = new TreeMap<String, AccessConfigRule>();
    private HashSet<IAccessRule> globalRules = new HashSet<IAccessRule>();

    public AccessManager() {
        AccessManagerInitEvent event = new AccessManagerInitEvent();
        BotAPI.eventBus.postEvent(event);
    }

    public void cleanup() {
        this.configRules.clear();
        this.globalRules.clear();
    }

    public boolean canView(String[] location, RequestSource source) {
        for (IAccessRule rule : this.globalRules) {
            rule.updateRules();
            if (!rule.canAccess(source)) {
                return false;
            }
        }

        String currentPath = "/";
        int i = -1;
        do {
            i++;
            AccessConfigRule rule = this.configRules.get(currentPath);

            if (rule != null) {
                rule.updateRules();
                if (!rule.canAccess(source)) {
                    return false;
                }
            }
            else {
                File dir = new File(GitPlugin.webDirectory, currentPath);
                if (!dir.exists()) {
                    continue;
                }
                File file = new File(GitPlugin.webDirectory, currentPath + "access.cfg");
                rule = new AccessConfigRule(file, currentPath);
                this.configRules.put(currentPath, rule);
                if (!rule.canAccess(source)) {
                    return false;
                }
            }

            if (i < location.length) {
                currentPath += location[i] + "/";
            }
        }
        while (i < location.length);

        return true;
    }

    public void registerRule(IAccessRule rule) {
        this.globalRules.add(rule);
    }

}
