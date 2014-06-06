
package me.heldplayer.irc.base.java;

import java.lang.reflect.Constructor;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCUser;

public class SandboxedClassLoader extends SecureClassLoader {

    private static final Logger log = Logger.getLogger("Sandbox");

    private HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

    private IRCUser user;
    protected ISandboxDelegate delegate;

    public SandboxedClassLoader(IRCUser user) {
        super(SandboxedClassLoader.class.getClassLoader());
        this.user = user;
        try {
            Class<?> delegateClass = this.findClass(SandboxDelegate.class.getCanonicalName());
            if (delegateClass == null) {
                throw new JavaException("ClassLoader isn't working!");
            }
            Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor(IExpressionEvaluator.class);
            delegateConstructor.setAccessible(true);
            Class<?> evaluatorClass = this.findClass(JavaExpressionEvaluator.class.getCanonicalName());
            if (evaluatorClass == null) {
                throw new JavaException("ClassLoader isn't working!");
            }
            Constructor<?> evaluatorConstructor = evaluatorClass.getDeclaredConstructor(IRCUser.class);
            evaluatorConstructor.setAccessible(true);
            this.delegate = (ISandboxDelegate) delegateConstructor.newInstance(evaluatorConstructor.newInstance(user));
        }
        catch (JavaException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new JavaException(e);
        }
    }

    private static Set<String> loaderExceptions = new HashSet<String>();
    static {
        loaderExceptions.add(ISandboxDelegate.class.getCanonicalName());
        loaderExceptions.add(IExpressionEvaluator.class.getCanonicalName());
        loaderExceptions.add(IRCUser.class.getCanonicalName());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (loaderExceptions.contains(name)) {
            return BotAPI.pluginLoader.findClass(name);
        }

        Class<?> result = classes.get(name);

        if (result == null) {
            byte[] bytes = BotAPI.pluginLoader.findBytes(name);

            try {
                result = super.defineClass(name, bytes, 0, bytes.length);
            }
            catch (Throwable e) {
                e.printStackTrace();
                return null;
            }

            SandboxedClassLoader.log.info(String.format("[%s] Loaded class '%s'", this.user.getUsername(), name));

            classes.put(name, result);
        }

        return result;
    }

    Set<String> getClasses() {
        return this.classes.keySet();
    }

}
