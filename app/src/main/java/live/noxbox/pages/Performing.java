package live.noxbox.pages;

import android.app.Activity;
import android.content.Intent;

import live.noxbox.model.Profile;
import live.noxbox.performing.PerformingActivity;
import live.noxbox.state.State;

public class Performing implements State {

    private Activity activity;

    public Performing(Activity activity) { this.activity = activity; }

    @Override
    public void draw(Profile profile) { activity.startActivity(new Intent(activity, PerformingActivity.class)); }

    @Override
    public void clear() { }
}
