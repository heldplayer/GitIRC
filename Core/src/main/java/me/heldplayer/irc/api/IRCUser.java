
package me.heldplayer.irc.api;

import java.util.UUID;

public class IRCUser {

    private String username;
    public char[] userModes = new char[0];
    private UUID uuid;

    public IRCUser(String username) {
        this.username = username;
        this.uuid = UUID.randomUUID();
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        IRCUser other = (IRCUser) obj;
        if (this.uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        }
        else if (!this.uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

}
