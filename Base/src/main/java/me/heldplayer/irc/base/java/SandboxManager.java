
package me.heldplayer.irc.base.java;

import java.util.HashMap;

import me.heldplayer.irc.api.IRCChannel;
import me.heldplayer.irc.api.IRCUser;

public final class SandboxManager {

    public static HashMap<IRCUser, SandboxedClassLoader> sandboxes = new HashMap<IRCUser, SandboxedClassLoader>();

    public static ISandboxDelegate getSandbox(IRCUser user) {
        SandboxedClassLoader loader = SandboxManager.sandboxes.get(user);
        return loader == null ? null : loader.delegate;
    }

    public static boolean removeSandbox(IRCUser user) {
        SandboxedClassLoader loader = SandboxManager.sandboxes.remove(user);
        loader.running = false;
        System.gc();
        return loader == null ? false : true;
    }

    public static ISandboxDelegate createSandbox(IRCUser user, IRCChannel channel) {
        if (SandboxManager.sandboxes.containsKey(user)) {
            throw new JavaException("Sandbox already exists for %s", user.getUsername());
        }

        final SandboxedClassLoader sandbox = new SandboxedClassLoader(user, channel);

        Thread executor = sandbox.delegate.setEvaluatorThread(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (sandbox.running) {
                        try {
                            sandbox.delegate.run();
                        }
                        catch (Throwable e) {
                            e.printStackTrace();
                        }

                        Thread.sleep(50L);
                    }
                }
                catch (InterruptedException e) {
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
        for (SandboxedClassLoader loader : SandboxManager.sandboxes.values()) {
            loader.running = false;
        }
        SandboxManager.sandboxes.clear();

        System.gc();
    }

}
