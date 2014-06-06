
package me.heldplayer.irc.base.java;

import java.util.LinkedList;
import java.util.TreeMap;

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

    // package.name.ClassName -> Return virtualized Class<?> clazz
    // clazz.class -> return actual Class<?> object
    // clazz.field -> return static field Object obj
    // clazz.method(params...) -> call static method and return Object obj
    // new package.name.ClassName(params...) -> create new instance and return Object instance
    // variable -> Return variable
    // variable.field -> return field from variable Object obj
    // variable.method(params...) -> call method from variable and return Object obj

    // Variables
    // Setting syntax: set variable, ... = ...
    // Unsetting syntax: unset variable, ...
    // Getting syntax (without using it for anything else): get variable, ...

    // Lookup order:
    // 1. variables
    // 2. "imported" classes
    // 3. classes/packages

    // Class importing
    // import package.name.ClassName -> Saves ClassName in importedClasses or errors if already imported

    protected final TreeMap<String, Class<?>> classes = new TreeMap<String, Class<?>>();
    protected final TreeMap<String, Class<?>> importedClasses = new TreeMap<String, Class<?>>();
    protected final TreeMap<String, Class<?>> methods = new TreeMap<String, Class<?>>();
    protected final TreeMap<String, Class<?>> fields = new TreeMap<String, Class<?>>();

    protected final TreeMap<String, Object> variables = new TreeMap<String, Object>();

}
