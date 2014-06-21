
package me.heldplayer.irc.base.java;

import java.lang.reflect.Constructor;

import me.heldplayer.irc.api.IRCChannel;
import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.sandbox.SandboxedClassLoader;

public class SandboxedClassLoaderImpl extends SandboxedClassLoader {

    private IRCUser user;
    protected ISandboxDelegate delegate;
    protected boolean running = true;

    public SandboxedClassLoaderImpl(IRCUser user, IRCChannel channel) {
        super();
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
            Constructor<?> evaluatorConstructor = evaluatorClass.getDeclaredConstructor(IMessageTarget.class);
            evaluatorConstructor.setAccessible(true);
            this.delegate = (ISandboxDelegate) delegateConstructor.newInstance(evaluatorConstructor.newInstance(new SimpleMessageTarget(user, channel)));
        }
        catch (JavaException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new JavaException(e);
        }
    }

    static {
        SandboxedClassLoader.loaderExceptions.add(ISandboxDelegate.class.getCanonicalName());
        SandboxedClassLoader.loaderExceptions.add(IExpressionEvaluator.class.getCanonicalName());
        SandboxedClassLoader.loaderExceptions.add(IMessageTarget.class.getCanonicalName());
    }

    @Override
    public String getName() {
        return this.user.getUsername();
    }

}
