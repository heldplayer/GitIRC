
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
        try {
            JavaExpression result = new JavaExpression(expression);
            result.execute(this);
        }
        catch (Throwable e) {
            e.printStackTrace();
            this.printString(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public void printString(String message) {
        BotAPI.serverConnection.addToSendQueue("PRIVMSG %s :%s: %s", "#specialattack", this.user.getUsername(), message);
    }

}
