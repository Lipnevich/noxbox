package live.noxbox.pages;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.model.Profile;
import live.noxbox.state.State;

public class Moving implements State {


    private GoogleMap googleMap;
    private Activity activity;

    public Moving(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(Profile profile) {

    }

    @Override
    public void clear() {

    }
}
