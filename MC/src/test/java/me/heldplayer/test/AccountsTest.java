
package me.heldplayer.test;

import java.util.UUID;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;

public class AccountsTest {

    public static void main(String[] args) {
        HttpProfileRepository repository = new HttpProfileRepository("minecraft");

        Profile[] profiles = repository.findProfilesByNames("heldplayer");

        for (Profile profile : profiles) {
            StringBuilder id = new StringBuilder(profile.getId());
            id.insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-');
            UUID uuid = UUID.fromString(id.toString());
            System.out.println(String.format("%s = %s", profile.getName(), uuid));

            Profile other = repository.findProfileByUUID(uuid.toString().replaceAll("-", ""));

            if (other != null) {
                System.out.println(String.format("%s = %s", other.getId(), other.getName()));
            }
            else {
                System.out.println("Reverse lookup failed");
            }
        }

        System.out.println("Done");
    }

}
