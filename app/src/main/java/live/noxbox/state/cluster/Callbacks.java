package live.noxbox.state.cluster;

import android.support.annotation.NonNull;

public interface Callbacks {

    boolean onClusterClick(@NonNull Cluster<NoxboxMarker> cluster);

    boolean onClusterItemClick(@NonNull NoxboxMarker clusterItem);
}
