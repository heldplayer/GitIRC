package me.heldplayer.irc.git.internal.security;

import me.heldplayer.irc.git.GitPlugin;
import me.heldplayer.irc.git.RequestSource;
import me.heldplayer.irc.git.internal.security.rules.Rule;
import me.heldplayer.util.json.JSONException;
import me.heldplayer.util.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public class AccessConfigRule implements IAccessRule {

    private File file;
    private long lastChanged;

    private Rule rootRule;

    public AccessConfigRule(File ruleFile, String path) {
        GitPlugin.getLog().info("Created new accessrule for " + path);
        this.file = ruleFile;
        this.lastChanged = this.file.lastModified();

        this.read();
    }

    private void read() {
        if (this.file.exists() && this.file.isFile()) {
            JSONObject object = null;
            try {
                object = new JSONObject(this.file);
            } catch (FileNotFoundException e) {
                throw new JSONException("Failed reading access rule", e);
            }

            JSONObject root = object.getObject("root");

            if (root != null) {
                this.rootRule = AccessManager.createRule(root);
            }
        }
    }

    @Override
    public void updateRules() {
        if (this.file.exists() && this.file.isFile()) {
            if (this.lastChanged < this.file.lastModified()) {
                this.read();
            }
        }
    }

    @Override
    public boolean canAccess(RequestSource source) {
        if (this.rootRule != null) {
            return this.rootRule.checkAccess(source);
        }
        return true;
    }

}
