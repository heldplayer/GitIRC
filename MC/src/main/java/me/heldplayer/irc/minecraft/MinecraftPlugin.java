
package me.heldplayer.irc.minecraft;

import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.event.EventHandler;
import me.heldplayer.irc.api.plugin.Plugin;
import me.heldplayer.irc.base.event.user.UserCommandEvent;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;

public class MinecraftPlugin extends Plugin {

    private static MinecraftPlugin instance;

    public static Logger getLog() {
        return MinecraftPlugin.instance.getLogger();
    }

    @Override
    public void onEnable() {
        MinecraftPlugin.instance = this;

        BotAPI.eventBus.registerEventHandler(this);
    }

    @Override
    public void onDisable() {}

    @EventHandler
    public void onUserCommand(UserCommandEvent event) {
        if (event.command.equals("MINECRAFT")) {
            event.setHandled();
            String[] params = event.getParams();

            if (params.length == 0) {
                BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel.getName(), event.user.getUsername(), "Usage: minecraft uuid/name");
            }
            else if (params.length == 1) {
                if (params[0].equalsIgnoreCase("uuid")) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel.getName(), event.user.getUsername(), "Usage: minecraft uuid [name]");
                }
                else if (params[0].equalsIgnoreCase("name")) {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel.getName(), event.user.getUsername(), "Usage: minecraft name [UUID]");
                }
                else {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel.getName(), event.user.getUsername(), "Usage: minecraft uuid/name");
                }
            }
            else {
                if (params[0].equalsIgnoreCase("uuid")) {
                    HttpProfileRepository repository = new HttpProfileRepository("minecraft");

                    for (int i = 1; i < params.length; i++) {
                        String name = params[i];
                        Profile[] profiles = repository.findProfilesByNames(name);
                        if (profiles.length == 0) {
                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Did not find a profile for '%s'", event.channel.getName(), event.user.getUsername(), name);
                        }
                        else {
                            for (Profile profile : profiles) {
                                BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: '%s' = '%s'", event.channel.getName(), event.user.getUsername(), profile.getName(), profile.getId());
                            }
                        }
                    }
                }
                else if (params[0].equalsIgnoreCase("name")) {
                    HttpProfileRepository repository = new HttpProfileRepository("minecraft");

                    for (int i = 1; i < params.length; i++) {
                        String string = params[i];
                        Profile profile = repository.findProfileByUUID(string.replaceAll("-", ""));
                        if (profile == null) {
                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: Did not find a profile for '%s'", event.channel.getName(), event.user.getUsername(), string);
                        }
                        else {
                            BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: '%s' = '%s'", event.channel.getName(), event.user.getUsername(), profile.getId(), profile.getName());
                        }
                    }
                }
                else {
                    BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", event.channel.getName(), event.user.getUsername(), "Usage: minecraft uuid/name");
                }
            }
        }
    }

}
