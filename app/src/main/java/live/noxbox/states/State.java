package live.noxbox.states;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.MapActivity;

public interface State {
    String TAG = "State: ";
    void draw(GoogleMap googleMap, MapActivity activity);
    void clear();
    void clearHandlers();

}
