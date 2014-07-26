package me.heldplayer.irc.base.java;

import me.heldplayer.irc.base.java.parts.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.TreeMap;

public class JavaExpressionEvaluator implements IExpressionEvaluator {

    protected final TreeMap<String, Class<?>> classes = new TreeMap<String, Class<?>>();
    protected final TreeMap<String, Class<?>> importedClasses = new TreeMap<String, Class<?>>();
    protected final TreeMap<String, TreeMap<String, Method>> methods = new TreeMap<String, TreeMap<String, Method>>();
    protected final TreeMap<String, TreeMap<String, Field>> fields = new TreeMap<String, TreeMap<String, Field>>();
    protected final TreeMap<String, Object> variables = new TreeMap<String, Object>();
    private IMessageTarget target;

    protected JavaExpressionEvaluator(IMessageTarget target) {
        this.target = target;
    }

    @Override
    public void evaluateExpression(String expression, ISandboxDelegate delegate) {
        try {
            JavaExpression result = new JavaExpression(expression);
            this.evaluate(result.value);
        } catch (Throwable e) {
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

    private Object evaluate(JavaPart part) {
        if (part instanceof StatementPart) {
            if (part.child == null) {
                this.printString("Statement is missing expression");
                return null;
            } else {
                StatementPart statement = (StatementPart) part;
                if (statement.name.equals("import")) {
                    String name = statement.child.toString();
                    if (classes.containsKey(name)) {
                        Class<?> clazz = classes.get(name);
                        this.importClass(clazz);
                        this.printString("Class '%s' imported as '%s'", name, clazz.getSimpleName());
                        return clazz;
                    }
                    Stack<Class<?>> stack = this.findClass(statement.child, true);
                    if (stack == null) {
                        throw new JavaException("Unknown Class");
                    } else {
                        this.classes.put(stack.value.getName(), stack.value);
                        this.importClass(stack.value);
                        this.printString("Class '%s' imported as '%s'", name, stack.value.getSimpleName());
                    }
                    return stack.value;
                } else if (statement.name.equals("unimport")) {
                    String name = statement.child.toString();

                    if (this.importedClasses.containsKey(name)) {
                        this.importedClasses.remove(name);
                        this.printString("Unimported '%s'", name);
                    } else {
                        throw new JavaException("A class with name '%s' hasn't been imported yet", name);
                    }
                    return null;
                } else {
                    throw new JavaException("Unknown statement");
                }
            }
        } else if (part instanceof StringPart) {
            this.printString("%s", part.toString());
            return ((StringPart) part).value;
        } else if (part instanceof NamedPart) {
            JavaPart currentPart = part.getRoot();
            Object obj = null;
            search:
            {
                if (currentPart instanceof NamedPart) {
                    String name = currentPart.toString();
                    if (this.variables.containsKey(name)) {
                        obj = this.variables.get(name);
                        break search;
                    }

                    if (this.importedClasses.containsKey(name)) {
                        obj = this.importedClasses.get(name);
                        break search;
                    }
                }
                if (currentPart instanceof StringPart) {
                    obj = ((StringPart) currentPart).value;
                    break search;
                }

                Stack<Class<?>> stack = this.findClass(part, false);
                if (stack != null) { // This is referencing a class
                    obj = stack.value;
                    currentPart = stack.part;
                    break search;
                }

                throw new JavaException("Unknown variable or Class '%s'", part.toString());
            }

            try {
                while (currentPart.child != null) {
                    JavaPart child = currentPart.child;
                    if (child instanceof FieldArrayPart) {
                        Field field;
                        if (obj instanceof Class) {
                            field = this.getField(((Class<?>) obj).getName(), ((FieldArrayPart) child).name);
                        } else {
                            field = this.getField(obj.getClass().getName(), ((FieldArrayPart) child).name);
                        }
                        if (field == null) {
                            throw new JavaException("Unknown field '%s'", ((FieldPart) child).name);
                        }
                        Object array;
                        if (obj instanceof Class) {
                            array = field.get(null);
                        } else {
                            array = field.get(obj);
                        }
                        obj = Array.get(array, ((FieldArrayPart) child).index);
                        currentPart = currentPart.child;
                        continue;
                    } else if (child instanceof FieldPart || child instanceof PartialAccessPart) {
                        Field field;
                        if (obj instanceof Class) {
                            field = this.getField(((Class<?>) obj).getName(), ((NamedPart) child).name);
                        } else {
                            field = this.getField(obj.getClass().getName(), ((NamedPart) child).name);
                        }
                        if (field == null) {
                            throw new JavaException("Unknown field '%s'", ((NamedPart) child).name);
                        }
                        if (obj instanceof Class) {
                            obj = field.get(null);
                        } else {
                            obj = field.get(obj);
                        }
                        currentPart = currentPart.child;
                        continue;
                    } else if (child instanceof MethodPart) {
                        MethodPart methodPart = (MethodPart) child;
                        Class<?>[] paramTypes = new Class<?>[methodPart.params.size()];
                        Object[] params = new Object[methodPart.params.size()];
                        for (int i = 0; i < paramTypes.length; i++) {
                            JavaPart paramPart = methodPart.params.get(i);
                            Object param = this.evaluate(paramPart);

                            params[i] = param;
                            paramTypes[i] = param.getClass();
                        }
                        Method method;
                        if (obj instanceof Class) {
                            method = this.getMethod(((Class<?>) obj).getName(), ((NamedPart) child).name, paramTypes);
                        } else {
                            method = this.getMethod(obj.getClass().getName(), ((NamedPart) child).name, paramTypes);
                        }
                        if (method == null) {
                            throw new JavaException("Unknown method '%s'", ((NamedPart) child).name);
                        }
                        if (obj instanceof Class) {
                            obj = method.invoke(null, (Object[]) params);
                        } else {
                            obj = method.invoke(obj, (Object[]) params);
                        }
                        currentPart = currentPart.child;
                        continue;
                    }

                    break;
                }
            } catch (Throwable e) {
                throw new JavaException(e);
            }

            this.printString("%s: %s", currentPart, currentPart.getClass());
            this.printString("%s: %s", obj, obj == null ? null : obj.getClass());
            return obj;
        } else {
            this.printString("%s: %s", part.toString(), part.getClass());
            return null;
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

    private Stack<Class<?>> findClass(JavaPart part, boolean classOnly) {
        Class<?> clazz = null;

        try {
            clazz = this.getClass().getClassLoader().loadClass(part.toString());
        } catch (ClassNotFoundException e) {
        }

        if (part.parent != null) {
            Stack<Class<?>> otherClass = this.findClass(part.parent, classOnly);
            if (clazz != null) {
                if (classOnly && part.child != null) {
                    throw new JavaException("Found class but not all parts were consumed");
                } else if (otherClass != null) {
                    return otherClass;
                }
                return new Stack<Class<?>>(part, clazz);
            } else {
                if (otherClass != null) {
                    return otherClass;
                }
            }
        } else {
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
        } else {
            this.importedClasses.put(simpleName, clazz);
        }
    }

    private Class<?> getClass(String className) {
        if (className == null) {
            return null;
        }
        if (this.classes.containsKey(className)) {
            return this.classes.get(className);
        }
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
            this.classes.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    private Method getMethod(String className, String methodName, Class<?>... params) {
        if (className == null || methodName == null) {
            return null;
        }
        if (this.methods.containsKey(className)) {
            TreeMap<String, Method> methods = this.methods.get(className);
            Method method = methods.get(methodName);
            if (method == null) {
                Class<?> clazz = this.getClass(className);
                if (clazz != null) {
                    try {
                        method = clazz.getDeclaredMethod(methodName, params);
                        methods.put(methodName, method);
                        method.setAccessible(true);
                    } catch (Throwable e) {
                    }
                }
            }
            return method;
        } else {
            Class<?> clazz = this.getClass(className);
            if (clazz != null) {
                TreeMap<String, Method> methods = new TreeMap<String, Method>();
                this.methods.put(className, methods);
                try {
                    Method method = clazz.getDeclaredMethod(methodName, params);
                    methods.put(methodName, method);
                    method.setAccessible(true);
                    return method;
                } catch (Throwable e) {
                }
            }
        }
        return null;
    }

    private Field getField(String className, String fieldName) {
        if (className == null || fieldName == null) {
            return null;
        }
        if (this.fields.containsKey(className)) {
            TreeMap<String, Field> fields = this.fields.get(className);
            Field field = fields.get(fieldName);
            if (field == null) {
                Class<?> clazz = this.getClass(className);
                if (clazz != null) {
                    try {
                        field = clazz.getDeclaredField(fieldName);
                        fields.put(fieldName, field);
                        field.setAccessible(true);
                    } catch (Throwable e) {
                    }
                }
            }
            return field;
        } else {
            Class<?> clazz = this.getClass(className);
            if (clazz != null) {
                TreeMap<String, Field> fields = new TreeMap<String, Field>();
                this.fields.put(className, fields);
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    fields.put(fieldName, field);
                    field.setAccessible(true);
                    return field;
                } catch (Throwable e) {
                }
            }
        }
        return null;
    }

    public static class Stack<T> {

        public T value;
        public JavaPart part;

        public Stack(JavaPart part, T value) {
            this.part = part;
            this.value = value;
        }

    }

}
