package by.nicolay.lipnevich.noxbox.state;

import by.nicolay.lipnevich.noxbox.model.Profile;

public interface State {

    void draw(Profile profile);
    void clear();

}
