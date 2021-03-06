package live.noxbox.cluster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.activities.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.MAX_ZOOM_LEVEL;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.Router.startActivity;

public class ClusterRenderer implements GoogleMap.OnMarkerClickListener {

    private static final int BACKGROUND_MARKER_Z_INDEX = 0;
    private static final int FOREGROUND_MARKER_Z_INDEX = 1;

    private GoogleMap googleMap;
    private Activity activity;
    private IconGenerator iconGenerator;

    private final Map<Cluster<NoxboxMarker>, Marker> markers = new HashMap<>();

    public ClusterRenderer(Activity activity, GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.activity = activity;
        this.iconGenerator = new IconGenerator(activity);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object markerTag = marker.getTag();
        if (markerTag instanceof Cluster) {
            Cluster<NoxboxMarker> cluster = (Cluster<NoxboxMarker>) marker.getTag();
            List<NoxboxMarker> clusterItems = cluster.getItems();

            if (clusterItems.size() > 1) {
                return onClusterClick(cluster);
            } else {
                return onClusterItemClick(clusterItems.get(0));
            }
        }

        //created decor
        if (markerTag instanceof Noxbox) {
            startActivity(activity, ContractActivity.class);
        }

        return false;
    }

    private boolean onClusterClick(@NonNull Cluster<NoxboxMarker> cluster) {
        float newZoom = googleMap.getCameraPosition().zoom + 1f;
        if (newZoom < MAX_ZOOM_LEVEL) {
            LatLng center = MapOperator.getCenterBetweenSomeLocations(cluster.getItems());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, newZoom));
        } else {
            ClusterItemsActivity.noxboxes.clear();
            ClusterItemsActivity.noxboxes.addAll(cluster.getItems());
            Router.startActivityForResult(activity, new Intent(activity, ClusterItemsActivity.class), 1);
        }
        return false;
    }

    private boolean onClusterItemClick(@NonNull final NoxboxMarker clusterItem) {
        if (googleMap.getCameraPosition() != null && googleMap.getCameraPosition().target != null) {
            profile().setPosition(Position.from(googleMap.getCameraPosition().target));
        }
        profile().setViewed(clusterItem.getNoxbox());
        Router.startActivity(activity, DetailedActivity.class);
        return true;
    }

    void render(@NonNull List<Cluster<NoxboxMarker>> clusters) {
        List<Cluster<NoxboxMarker>> clustersToAdd = new ArrayList<>();
        List<Cluster<NoxboxMarker>> clustersToRemove = new ArrayList<>();

        for (Cluster<NoxboxMarker> cluster : clusters) {
            if (!markers.containsKey(cluster)) {
                clustersToAdd.add(cluster);
            }
        }

        for (Cluster<NoxboxMarker> cluster : markers.keySet()) {
            if (!clusters.contains(cluster)) {
                clustersToRemove.add(cluster);
            }
        }

        clusters.addAll(clustersToAdd);
        clusters.removeAll(clustersToRemove);

        // Remove the old clusters.
        for (Cluster<NoxboxMarker> clusterToRemove : clustersToRemove) {
            Marker markerToRemove = markers.get(clusterToRemove);
            markerToRemove.setZIndex(BACKGROUND_MARKER_Z_INDEX);

            Cluster<NoxboxMarker> parentCluster = findParentCluster(clusters, clusterToRemove.getLatitude(),
                    clusterToRemove.getLongitude());
            if (parentCluster != null) {
                animateMarkerToLocation(markerToRemove, new LatLng(parentCluster.getLatitude(),
                        parentCluster.getLongitude()), true);
            } else {
                markerToRemove.remove();
            }

            markers.remove(clusterToRemove);
        }

        // Add the new clusters.
        for (Cluster<NoxboxMarker> clusterToAdd : clustersToAdd) {
            Marker markerToAdd;


            Cluster parentCluster = findParentCluster(clustersToRemove, clusterToAdd.getLatitude(),
                    clusterToAdd.getLongitude());
            if (parentCluster != null) {
                markerToAdd = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(parentCluster.getLatitude(), parentCluster.getLongitude()))
                        .icon(getMarkerIcon(clusterToAdd))
                        .zIndex(FOREGROUND_MARKER_Z_INDEX));
                animateMarkerToLocation(markerToAdd,
                        new LatLng(clusterToAdd.getLatitude(), clusterToAdd.getLongitude()), false);
            } else {
                markerToAdd = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(clusterToAdd.getLatitude(), clusterToAdd.getLongitude()))
                        .icon(getMarkerIcon(clusterToAdd))
                        .alpha(0.0F)
                        .zIndex(FOREGROUND_MARKER_Z_INDEX));
                animateMarkerAppearance(markerToAdd);
            }
            markerToAdd.setTag(clusterToAdd);
            markerToAdd.setTitle(clusterToAdd.getLatitude() + "" + clusterToAdd.getLongitude());

            markers.put(clusterToAdd, markerToAdd);
        }
    }

    @NonNull
    private BitmapDescriptor getMarkerIcon(@NonNull Cluster<NoxboxMarker> cluster) {
        BitmapDescriptor clusterIcon;
        List<NoxboxMarker> clusterItems = cluster.getItems();
        if (clusterItems.size() > 1) {
            clusterIcon = iconGenerator.getClusterIcon(cluster);
        } else {
            clusterIcon = iconGenerator.getClusterItemIcon(clusterItems.get(0));
        }

        return clusterIcon;
    }

    @Nullable
    private Cluster<NoxboxMarker> findParentCluster(@NonNull List<Cluster<NoxboxMarker>> clusters,
                                                    double latitude, double longitude) {
        for (Cluster<NoxboxMarker> cluster : clusters) {
            if (cluster.contains(latitude, longitude)) {
                return cluster;
            }
        }

        return null;
    }

    private void animateMarkerToLocation(@NonNull final Marker marker, @NonNull LatLng targetLocation,
                                         final boolean removeAfter) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofObject(marker, "position",
                new LatLngTypeEvaluator(), targetLocation);
        objectAnimator.setInterpolator(new FastOutSlowInInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (removeAfter) {
                    marker.remove();
                }
            }
        });
        objectAnimator.start();
    }

    private void animateMarkerAppearance(@NonNull Marker marker) {
        ObjectAnimator.ofFloat(marker, "alpha", 1.0F).start();
    }

    public void clear() {
        markers.clear();
    }

    private static class LatLngTypeEvaluator implements TypeEvaluator<LatLng> {

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            double latitude = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude;
            double longitude = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude;
            return new LatLng(latitude, longitude);
        }
    }
}
