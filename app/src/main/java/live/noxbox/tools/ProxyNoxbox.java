package live.noxbox.tools;

import live.noxbox.state.ProfileStorage;

public class ProxyNoxbox {
    public void callTask(){
        ProfileStorage.fireProfile();
    }
}
