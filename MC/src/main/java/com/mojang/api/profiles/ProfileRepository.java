
package com.mojang.api.profiles;

public interface ProfileRepository {

    public Profile[] findProfilesByNames(String... names);

    public Profile findProfileByUUID(String UUID);

}
