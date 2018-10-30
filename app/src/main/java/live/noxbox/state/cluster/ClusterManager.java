package live.noxbox.state.cluster;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.tools.MapController;

import static live.noxbox.tools.DetectNullValue.areNotTheyNull;

public class ClusterManager implements GoogleMap.OnCameraIdleListener {

    private static final int QUAD_TREE_BUCKET_CAPACITY = 4;
    private static final int DEFAULT_MIN_CLUSTER_SIZE = 1;

    private GoogleMap googleMap;
    private Context context;
    private live.noxbox.state.cluster.ClusterRenderer renderer;

    private AsyncTask quadTreeTask;
    private AsyncTask clusterTask;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final QuadTree quadTree = new QuadTree(QUAD_TREE_BUCKET_CAPACITY);

    private int minClusterSize = DEFAULT_MIN_CLUSTER_SIZE;

    public ClusterManager(@NonNull Context context, @NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.context = context;
        this.renderer = new live.noxbox.state.cluster.ClusterRenderer(this.context, googleMap);
    }

    public ClusterManager setCallbacks(@Nullable Callbacks callbacks) {
        renderer.setCallbacks(callbacks);
        return this;
    }

    public void setItems(@NonNull Map<String, Noxbox> noxboxItems, final Profile profile) {
        List<NoxboxMarker> clusterItems = new ArrayList<>();
        if (areNotTheyNull(noxboxItems)) {
            for (Noxbox noxbox : noxboxItems.values()) {
                if (isFiltered(profile, noxbox)) continue;
                clusterItems.add(new NoxboxMarker(noxbox.getPosition().toLatLng(), noxbox));
            }
            buildQuadTree(clusterItems);
        }
    }

    private boolean isFiltered(Profile profile, Noxbox noxbox) {

        //TODO (vl) так же проверять время, оно должно совпадать с рабочими часами

        if (!profile.getFilters().getAllowNovices())
            return true;

        if (profile.getDarkList().get(noxbox.getOwner().getId()) != null)
            return true;
        if (!profile.getFilters().getTypes().get(noxbox.getType().name()))
            return true;
        if (Integer.parseInt(noxbox.getPrice()) > Integer.parseInt(profile.getFilters().getPrice()))
            return true;
        if (noxbox.getRole() == MarketRole.demand && !profile.getFilters().getDemand())
            return true;

        if (noxbox.getRole() == MarketRole.supply && !profile.getFilters().getSupply())
            return true;

        //фильтры по типу передвижения
        if (noxbox.getOwner().getHost() && noxbox.getOwner().getTravelMode() == TravelMode.none) {
            if (profile.getTravelMode() == TravelMode.none)
                return true;
        }

        if (!noxbox.getOwner().getHost()) {
            return !profile.getHost();
        }
        return false;
    }

    private void buildQuadTree(@NonNull List<NoxboxMarker> clusterItems) {
        if (quadTreeTask != null) {
            quadTreeTask.cancel(true);
        }

        quadTreeTask = new QuadTreeTask(clusterItems).executeOnExecutor(executor);
    }

    @Override
    public void onCameraIdle() {
        cluster();
    }


    private void cluster() {
        if (clusterTask != null) {
            clusterTask.cancel(true);
        }

        clusterTask = new ClusterTask(googleMap.getProjection().getVisibleRegion().latLngBounds,
                googleMap.getCameraPosition().zoom).executeOnExecutor(executor);
    }

    @NonNull
    private List<live.noxbox.state.cluster.Cluster<NoxboxMarker>> getClusters
            (@NonNull LatLngBounds latLngBounds, float zoomLevel) {
        List<live.noxbox.state.cluster.Cluster<NoxboxMarker>> clusters = new ArrayList<>();

        long tileCount = (long) (Math.pow(2, zoomLevel) * 2);

        double startLatitude = latLngBounds.northeast.latitude;
        double endLatitude = latLngBounds.southwest.latitude;

        double startLongitude = latLngBounds.southwest.longitude;
        double endLongitude = latLngBounds.northeast.longitude;

        double stepLatitude = 180.0 / tileCount;
        double stepLongitude = 360.0 / tileCount;

        if (startLongitude > endLongitude) { // Longitude +180°/-180° overlap.
            // [start longitude; 180]
            getClustersInsideBounds(clusters, startLatitude, endLatitude,
                    startLongitude, 180.0, stepLatitude, stepLongitude);
            // [-180; end longitude]
            getClustersInsideBounds(clusters, startLatitude, endLatitude,
                    -180.0, endLongitude, stepLatitude, stepLongitude);
        } else {
            getClustersInsideBounds(clusters, startLatitude, endLatitude,
                    startLongitude, endLongitude, stepLatitude, stepLongitude);
        }

        return clusters;
    }

    private void getClustersInsideBounds
            (@NonNull List<live.noxbox.state.cluster.Cluster<NoxboxMarker>> clusters,
             double startLatitude, double endLatitude,
             double startLongitude, double endLongitude,
             double stepLatitude, double stepLongitude) {
        long startX = (long) ((startLongitude + 180.0) / stepLongitude);
        long startY = (long) ((90.0 - startLatitude) / stepLatitude);

        long endX = (long) ((endLongitude + 180.0) / stepLongitude) + 1;
        long endY = (long) ((90.0 - endLatitude) / stepLatitude) + 1;

        for (long tileX = startX; tileX <= endX; tileX++) {
            for (long tileY = startY; tileY <= endY; tileY++) {
                double north = 90.0 - tileY * stepLatitude;
                double west = tileX * stepLongitude - 180.0;
                double south = north - stepLatitude;
                double east = west + stepLongitude;

                List<NoxboxMarker> points = quadTree.queryRange(north, west, south, east);

                if (points.isEmpty()) {
                    continue;
                }

                if (points.size() >= minClusterSize) {

                    LatLng center = MapController.getCenterBetweenSomeLocations(points);
                    clusters.add(new live.noxbox.state.cluster.Cluster<NoxboxMarker>(center.latitude, center.longitude,
                            points, north, west, south, east));
                } else {
                    for (NoxboxMarker point : points) {
                        clusters.add(new live.noxbox.state.cluster.Cluster<NoxboxMarker>(point.getPosition().latitude, point.getPosition().longitude,
                                Collections.singletonList(point), north, west, south, east));
                    }
                }
            }
        }
    }

    private class QuadTreeTask extends AsyncTask<Void, Void, Void> {

        private final List<NoxboxMarker> mClusterItems;

        private QuadTreeTask(@NonNull List<NoxboxMarker> clusterItems) {
            mClusterItems = clusterItems;
        }

        @Override
        protected Void doInBackground(Void... params) {
            quadTree.clear();
            for (NoxboxMarker clusterItem : mClusterItems) {
                quadTree.insert(clusterItem);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            cluster();
            quadTreeTask = null;
        }
    }

    private class ClusterTask extends AsyncTask<Void, Void, List<live.noxbox.state.cluster.Cluster<NoxboxMarker>>> {

        private final LatLngBounds mLatLngBounds;
        private final float mZoomLevel;

        private ClusterTask(@NonNull LatLngBounds latLngBounds, float zoomLevel) {
            mLatLngBounds = latLngBounds;
            mZoomLevel = zoomLevel;
        }

        @Override
        protected List<live.noxbox.state.cluster.Cluster<NoxboxMarker>> doInBackground(Void... params) {
            return getClusters(mLatLngBounds, mZoomLevel);
        }

        @Override
        protected void onPostExecute(@NonNull List<live.noxbox.state.cluster.Cluster<NoxboxMarker>> clusters) {
            renderer.render(clusters);
            clusterTask = null;
        }
    }

    class QuadTree {

        private final int bucketSize;

        private QuadTreeNode root;

        QuadTree(int bucketSize) {
            this.bucketSize = bucketSize;
            this.root = createRootNode(bucketSize);
        }

        void insert(@NonNull NoxboxMarker point) {
            root.insert(point);
        }

        @NonNull
        List<NoxboxMarker> queryRange(double north, double west, double south, double east) {
            List<NoxboxMarker> points = new ArrayList<>();
            root.queryRange(new QuadTreeRect(north, west, south, east), points);
            return points;
        }

        void clear() {
            root = createRootNode(bucketSize);
        }

        @NonNull
        private QuadTreeNode createRootNode(int bucketSize) {
            return new QuadTreeNode(90.0, -180.0, -90.0, 180.0, bucketSize);
        }
    }


    class QuadTreeNode {

        private final QuadTreeRect bounds;
        private final List<NoxboxMarker> points;
        private final int bucketSize;
        private QuadTreeNode northWest;
        private QuadTreeNode northEast;
        private QuadTreeNode southWest;
        private QuadTreeNode southEast;

        QuadTreeNode(double north, double west, double south, double east, int bucketSize) {
            this.bounds = new QuadTreeRect(north, west, south, east);
            this.points = new ArrayList<>(bucketSize);
            this.bucketSize = bucketSize;
        }

        boolean insert(@NonNull NoxboxMarker point) {
            // Ignore objects that do not belong in this quad tree.
            if (!bounds.contains(point.getPosition().latitude, point.getPosition().longitude)) {
                return false;
            }

            // If there is space in this quad tree, add the object here.
            if (points.size() < bucketSize) {
                points.add(point);
                return true;
            }

            // Otherwise, subdivide and then add the point to whichever node will accept it.
            if (northWest == null) {
                subdivide();
            }

            if (northWest.insert(point)) {
                return true;
            }
            if (northEast.insert(point)) {
                return true;
            }
            if (southWest.insert(point)) {
                return true;
            }
            return southEast.insert(point);

            // Otherwise, the point cannot be inserted for some unknown reason (this should never happen).
        }

        void queryRange(@NonNull QuadTreeRect range, @NonNull List<NoxboxMarker> pointsInRange) {
            // Automatically abort if the range does not intersect this quad.
            if (!bounds.intersects(range)) {
                return;
            }

            // Check objects at this quad level.
            for (NoxboxMarker point : points) {
                if (range.contains(point.getPosition().latitude, point.getPosition().longitude)) {
                    pointsInRange.add(point);
                }
            }

            // Terminate here, if there are no children.
            if (northWest == null) {
                return;
            }

            // Otherwise, add the points from the children.
            northWest.queryRange(range, pointsInRange);
            northEast.queryRange(range, pointsInRange);
            southWest.queryRange(range, pointsInRange);
            southEast.queryRange(range, pointsInRange);
        }

        private void subdivide() {
            double northSouthHalf = bounds.north - (bounds.north - bounds.south) / 2.0;
            double eastWestHalf = bounds.east - (bounds.east - bounds.west) / 2.0;

            northWest = new QuadTreeNode(bounds.north, bounds.west, northSouthHalf, eastWestHalf, bucketSize);
            northEast = new QuadTreeNode(bounds.north, eastWestHalf, northSouthHalf, bounds.east, bucketSize);
            southWest = new QuadTreeNode(northSouthHalf, bounds.west, bounds.south, eastWestHalf, bucketSize);
            southEast = new QuadTreeNode(northSouthHalf, eastWestHalf, bounds.south, bounds.east, bucketSize);
        }
    }

    class QuadTreeRect {

        final double north;
        final double west;
        final double south;
        final double east;

        QuadTreeRect(double north, double west, double south, double east) {
            this.north = north;
            this.west = west;
            this.south = south;
            this.east = east;
        }

        boolean contains(double latitude, double longitude) {
            return longitude >= west && longitude <= east && latitude <= north && latitude >= south;
        }

        boolean intersects(@NonNull QuadTreeRect bounds) {
            return west <= bounds.east && east >= bounds.west && north >= bounds.south && south <= bounds.north;
        }
    }


}
