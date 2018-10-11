package live.noxbox.state;

import live.noxbox.model.Profile;

public interface State {

    void draw(Profile profile);
    void clear();

}
