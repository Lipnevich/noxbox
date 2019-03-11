package live.noxbox.cluster;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Objects;

import live.noxbox.model.Noxbox;

public class NoxboxMarker implements ClusterItem {
    private LatLng position;
    private Noxbox noxbox;

    public NoxboxMarker(LatLng position, final Noxbox noxbox) {
        this.position = position;
        this.noxbox = noxbox;

    }

    public Noxbox getNoxbox() {
        return noxbox;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoxboxMarker that = (NoxboxMarker) o;
        return Objects.equals(noxbox, that.noxbox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noxbox);
    }
}
