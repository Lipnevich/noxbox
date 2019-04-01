package live.noxbox.states.decorator;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.MapActivity;

/**
 * Created by Vladislaw Kravchenok on 01.04.2019.
 */
public abstract class StatesDecorator {
    protected GoogleMap googleMap;
    protected MapActivity activity;

    public abstract void draw(GoogleMap googleMap, MapActivity activity);
    public abstract void clear();
}
