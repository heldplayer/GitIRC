package me.heldplayer.test;

import me.heldplayer.irc.api.event.user.CommandEvent;

import java.util.Arrays;

public class CommandTest {

    public static void main(String[] args) {
        String input = "THIS is a :test wheee";
        CommandEvent event = new CommandEvent(input);

        System.out.println(event.command);
        System.out.println(Arrays.toString(event.params));
    }

}
