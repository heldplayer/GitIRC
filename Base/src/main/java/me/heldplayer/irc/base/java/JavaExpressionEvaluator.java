
package me.heldplayer.irc.base.java;

public class JavaExpressionEvaluator implements IExpressionEvaluator {

    private IMessageTarget target;

    protected JavaExpressionEvaluator(IMessageTarget target) {
        this.target = target;
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
        this.target.sendMessage(message);
    }

}
