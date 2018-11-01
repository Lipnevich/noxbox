package live.noxbox.state;

import live.noxbox.model.Profile;

public interface State {
    String TAG = "State: ";
    void draw(Profile profile);
    void clear();

}
