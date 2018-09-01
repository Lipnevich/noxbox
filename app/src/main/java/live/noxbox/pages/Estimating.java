package live.noxbox.pages;

import android.app.Activity;

import live.noxbox.model.Profile;
import live.noxbox.state.State;

public class Estimating implements State {

    private Activity activity;

    public Estimating(Activity activity) { this.activity = activity; }

    @Override
    public void draw(Profile profile) {

    }

    @Override
    public void clear() {

    }
}
