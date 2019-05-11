package live.noxbox.cluster;

import androidx.annotation.NonNull;

public interface Callbacks {

    boolean onClusterClick(@NonNull Cluster<NoxboxMarker> cluster);

    boolean onClusterItemClick(@NonNull NoxboxMarker clusterItem);
}
