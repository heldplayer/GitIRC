package me.heldplayer.irc.base.java;

public interface ISandboxDelegate {

    void run();

    void addCommand(String command);

    Thread setEvaluatorThread(Thread thread);

}
