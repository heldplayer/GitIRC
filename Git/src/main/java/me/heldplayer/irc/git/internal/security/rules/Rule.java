package me.heldplayer.irc.git.internal.security.rules;

import me.heldplayer.irc.git.RequestSource;

public interface Rule {

    boolean checkAccess(RequestSource source);

}
