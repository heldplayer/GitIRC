
package me.heldplayer.irc.base.java;

import java.util.TreeMap;

public class JavaExpressionEvaluator implements IExpressionEvaluator {

    private IMessageTarget target;

    protected JavaExpressionEvaluator(IMessageTarget target) {
        this.target = target;
    }

    @Override
    public void evaluateExpression(String expression, ISandboxDelegate delegate) {
        try {
            JavaExpression result = new JavaExpression(expression);
            this.evaluate(result.value);
        }
        catch (Throwable e) {
            e.printStackTrace();
            this.printString("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void printString(String message) {
        this.target.sendMessage(message);
    }

    @Override
    public void printString(String message, Object... params) {
        this.target.sendMessage(message, params);
    }

    private void evaluate(JavaPart part) {
        if (part instanceof StatementPart) {
            if (part.child == null) {
                this.printString("Statement is missing expression");
            }
            else {
                StatementPart statement = (StatementPart) part;
                if (statement.name.equals("import")) {
                    String name = statement.child.toString();
                    if (classes.containsKey(name)) {
                        Class<?> clazz = classes.get(name);
                        this.importClass(clazz);
                        this.printString("Class '%s' imported as '%s'", name, clazz.getSimpleName());
                        return;
                    }
                    Stack<Class<?>> stack = this.findClass(statement.child, true);
                    if (stack == null) {
                        throw new JavaException("Unknown Class");
                    }
                    else {
                        this.classes.put(stack.value.getName(), stack.value);
                        this.importClass(stack.value);
                        this.printString("Class '%s' imported as '%s'", name, stack.value.getSimpleName());
                    }
                }
                else if (statement.name.equals("unimport")) {
                    String name = statement.child.toString();

                    if (this.importedClasses.containsKey(name)) {
                        this.importedClasses.remove(name);
                        this.printString("Unimported '%s'", name);
                    }
                    else {
                        throw new JavaException("A class with name '%s' hasn't been imported yet", name);
                    }
                }
                else {
                    throw new JavaException("Unknown statement");
                }
            }
        }
        else {
            this.printString("%s: %s", part.toString(), part.getClass());
        }
    }

    private Stack<Class<?>> findClass(JavaPart part, boolean classOnly) {
        Class<?> clazz = null;

        try {
            clazz = this.getClass().getClassLoader().loadClass(part.toString());
        }
        catch (ClassNotFoundException e) {}

        if (part.parent != null) {
            Stack<Class<?>> otherClass = this.findClass(part.parent, classOnly);
            if (clazz != null) {
                if (otherClass != null && classOnly && part.child != null) {
                    throw new JavaException("Found class but not all parts were consumed");
                }
                else if (otherClass != null) {
                    return otherClass;
                }
                return new Stack<Class<?>>(part, clazz);
            }
            else {
                if (otherClass != null) {
                    return otherClass;
                }
            }
        }
        else {
            if (clazz != null) {
                return new Stack<Class<?>>(part, clazz);
            }
        }

        return null;
    }

    private void importClass(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();

        if (this.importedClasses.containsKey(simpleName)) {
            throw new JavaException("A class with name '%s' has already been imported", simpleName);
        }
        else {
            this.importedClasses.put(simpleName, clazz);
        }
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

    public static class Stack<T> {

        public T value;
        public JavaPart part;

        public Stack(JavaPart part, T value) {
            this.part = part;
            this.value = value;
        }

    }

}
