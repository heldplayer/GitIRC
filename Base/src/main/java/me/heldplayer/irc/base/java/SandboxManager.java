package me.heldplayer.irc.base.java;

import me.heldplayer.irc.api.IRCChannel;
import me.heldplayer.irc.api.IRCUser;

import java.util.HashMap;

public final class SandboxManager {

    public static HashMap<IRCUser, SandboxedClassLoaderImpl> sandboxes = new HashMap<IRCUser, SandboxedClassLoaderImpl>();

    public static ISandboxDelegate getSandbox(IRCUser user) {
        SandboxedClassLoaderImpl loader = SandboxManager.sandboxes.get(user);
        return loader == null ? null : loader.delegate;
    }

    public static boolean removeSandbox(IRCUser user) {
        SandboxedClassLoaderImpl loader = SandboxManager.sandboxes.remove(user);
        loader.running = false;
        System.gc();
        return loader == null ? false : true;
    }

    public static ISandboxDelegate createSandbox(IRCUser user, IRCChannel channel) {
        if (SandboxManager.sandboxes.containsKey(user)) {
            throw new JavaException("Sandbox already exists for %s", user.getUsername());
        }

        final SandboxedClassLoaderImpl sandbox = new SandboxedClassLoaderImpl(user, channel);

        Thread executor = sandbox.delegate.setEvaluatorThread(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (sandbox.running) {
                        try {
                            sandbox.delegate.run();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        Thread.sleep(50L);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Sandbox thread: " + user.getUsername()));

        executor.setContextClassLoader(sandbox);
        executor.setDaemon(true);
        executor.start();

        SandboxManager.sandboxes.put(user, sandbox);
        return sandbox.delegate;
    }

    public static void resetAll() {
        for (SandboxedClassLoaderImpl loader : SandboxManager.sandboxes.values()) {
            loader.running = false;
        }
        SandboxManager.sandboxes.clear();

        System.gc();
    }

}
