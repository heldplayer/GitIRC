
package me.heldplayer.web.server.internal.security;

import me.heldplayer.web.server.RequestSource;

public interface IAccessRule {

    void updateRules();

    boolean canAccess(RequestSource source);

}
