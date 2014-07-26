package me.heldplayer.irc.base.java;

public interface IExpressionEvaluator {

    void evaluateExpression(String expression, ISandboxDelegate delegate);

    void printString(String message);

    void printString(String message, Object... params);

}
