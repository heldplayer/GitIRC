package com.mojang.api.profiles;

import java.util.UUID;

public class Profile {

    private String id;
    private String name;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getUUID() {
        StringBuilder id = new StringBuilder(this.id);
        id.insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-');

        return UUID.fromString(id.toString());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
