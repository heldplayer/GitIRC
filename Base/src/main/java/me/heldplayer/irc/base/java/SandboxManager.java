
package me.heldplayer.irc.base.java;

import java.util.HashMap;

import me.heldplayer.irc.api.IRCUser;

public final class SandboxManager {

    public static HashMap<IRCUser, SandboxedClassLoader> sandboxes = new HashMap<IRCUser, SandboxedClassLoader>();

    public static ISandboxDelegate getSandbox(IRCUser user) {
        SandboxedClassLoader loader = sandboxes.get(user);
        return loader == null ? null : loader.delegate;
    }

    public static ISandboxDelegate createSandbox(IRCUser user) {
        if (sandboxes.containsKey(user)) {
            throw new JavaException("Sandbox already exists for " + user.getUsername());
        }

        final SandboxedClassLoader sandbox = new SandboxedClassLoader(user);

        Thread executor = sandbox.delegate.setEvaluatorThread(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        sandbox.delegate.run();
                        Thread.sleep(500L);
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

        sandboxes.put(user, sandbox);
        return sandbox.delegate;
    }

    public static void resetAll() {
        sandboxes.clear();
    }

}
