
package me.heldplayer.irc.base.java;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCUser;

public class JavaExpressionEvaluator implements IExpressionEvaluator {

    private IRCUser user;

    protected JavaExpressionEvaluator(IRCUser user) {
        this.user = user;
    }

    @Override
    public void evaluateExpression(String expression, ISandboxDelegate delegate) {
        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", "#specialattack", user.getUsername(), expression);
    }

    @Override
    public void printString(String message) {
        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", "#specialattack", user.getUsername(), message);
    }

}
