
package me.heldplayer.irc.git.internal.security;

import me.heldplayer.irc.git.RequestSource;

public interface IAccessRule {

    void updateRules();

    boolean canAccess(RequestSource source);

}
