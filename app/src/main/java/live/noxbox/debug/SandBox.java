package live.noxbox.debug;

import com.google.gson.Gson;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

public class SandBox {
    public static void main(String[] args) {
        Profile profile = new Profile();
        System.out.println(new Gson().toJson(profile));
        System.out.println(new Gson().toJson(profile).toString());

        Noxbox noxbox = new Noxbox();
        System.out.println(new Gson().toJson(noxbox));

        profile.setCurrent(noxbox);
        System.out.println(new Gson().toJson(profile));
        System.out.println(new Gson().toJson(noxbox));

        noxbox.setOwner(profile);
        System.out.println(new Gson().toJson(profile));
        System.out.println(new Gson().toJson(noxbox));
    }
}
