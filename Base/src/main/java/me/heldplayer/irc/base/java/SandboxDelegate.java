package me.heldplayer.irc.base.java;

import java.util.LinkedList;

public class SandboxDelegate implements ISandboxDelegate {

    /**
     * List of commands to process
     */
    protected LinkedList<String> queue = new LinkedList<String>();
    protected IExpressionEvaluator evaluator;
    protected Thread evaluatorThread;

    protected SandboxDelegate(IExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void run() {
        if (!this.queue.isEmpty()) {
            String line = this.queue.remove(0);
            this.evaluator.evaluateExpression(line, this);
        }
    }

    @Override
    public void addCommand(String command) {
        this.queue.add(command);
    }

    @Override
    public Thread setEvaluatorThread(Thread thread) {
        this.evaluatorThread = thread;
        return thread;
    }

}
