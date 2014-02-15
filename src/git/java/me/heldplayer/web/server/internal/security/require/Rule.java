
package me.heldplayer.web.server.internal.security.require;

import me.heldplayer.web.server.RequestSource;

public interface Rule {

    boolean checkAccess(RequestSource source);

}
