
package me.heldplayer.web.server.internal.security.rules;

import me.heldplayer.web.server.RequestSource;

public interface Rule {

    boolean checkAccess(RequestSource source);

}
