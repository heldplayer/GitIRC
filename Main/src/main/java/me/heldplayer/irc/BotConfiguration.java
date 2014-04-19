
package me.heldplayer.irc;

import java.io.File;

import me.heldplayer.irc.api.IConfiguration;
import me.heldplayer.irc.api.configuration.Configuration;

class BotConfiguration implements IConfiguration {

    private String serverIp;
    private int serverPort;
    private String nickname;
    private String bindHost;
    private String logFile;
    private String commandPrefix;

    public BotConfiguration() {
        Configuration config = new Configuration(new File("." + File.separator + "settings.cfg"));
        config.setDefault("server-ip", "localhost");
        config.setDefault("server-port", "6667");
        config.setDefault("nickname", "bot");
        config.setDefault("bind-host", "");
        config.setDefault("log-file", "./console.log");
        config.setDefault("command-prefix", "!");
        config.load();

        this.serverIp = config.getString("server-ip");
        this.serverPort = config.getInt("server-port");
        this.nickname = config.getString("nickname");
        this.bindHost = config.getString("bind-host");
        this.logFile = config.getString("log-file");
        this.commandPrefix = config.getString("command-prefix");
    }

    @Override
    public String getServerIp() {
        return serverIp;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getBindHost() {
        return bindHost;
    }

    @Override
    public String getLogFile() {
        return logFile;
    }

    @Override
    public String getCommandPrefix() {
        return commandPrefix;
    }

}
