package live.noxbox.states;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.MapActivity;
import live.noxbox.model.Profile;

public interface State {
    String TAG = "State: ";
    void draw(GoogleMap googleMap, MapActivity activity);
    void clear();

}
