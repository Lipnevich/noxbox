package live.noxbox.pages;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.filters.MapFiltersActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Task;

public class AvailableServices implements State, ClusterManager.OnClusterClickListener<AvailableServices.NoxboxMarker>,
        ClusterManager.OnClusterItemClickListener<AvailableServices.NoxboxMarker> {

    private Map<String, Marker> markers = new HashMap<>();
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private List<Noxbox> noxboxes;
    private ClusterManager<NoxboxMarker> clusterManager;
    private CustomClusterRenderer customClusterRenderer;


    public AvailableServices(GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;
        this.clusterManager = new ClusterManager<>(activity, googleMap);
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.filter).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, MapFiltersActivity.class));
            }
        });
        activity.findViewById(R.id.floatingButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.floatingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setPosition(Position.from(googleMap.getCameraPosition().target));
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    profile.getCurrent().getOwner().setPosition(Position.from(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                }
                profile.getCurrent().getOwner().setPhoto(profile.getPhoto());
                profile.getCurrent().getOwner().setName(profile.getName());

                activity.startActivity(new Intent(activity, ConstructorActivity.class));

            }
        });
        activity.findViewById(R.id.debugNoxboxExample).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.debugNoxboxExample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noxboxes == null) {
                    noxboxes = NoxboxExamples.generateNoxboxes(new Position().setLongitude(27.569018).setLatitude(53.871399), 150, profile);
                }
                googleMap.setOnMarkerClickListener(clusterManager);
                googleMap.setOnCameraIdleListener(clusterManager);
                customClusterRenderer = new CustomClusterRenderer(activity, googleMap, clusterManager);
                clusterManager.setOnClusterClickListener(AvailableServices.this);
                clusterManager.setOnClusterItemClickListener(AvailableServices.this);

                clusterManager.setRenderer(customClusterRenderer);
                for (Noxbox noxbox : noxboxes) {
                    if ((profile.getTravelMode() == TravelMode.none && noxbox.getOwner().getTravelMode() == TravelMode.none)
                            || (profile.getTravelMode() != TravelMode.none && noxbox.getOwner().getTravelMode() != TravelMode.none && !profile.getHost() && !noxbox.getOwner().getHost())) {
                        //do not show this marker
                    } else {
                        clusterManager.addItem(new NoxboxMarker(noxbox.getPosition().toLatLng(), noxbox));
                    }
                }
                clusterManager.cluster();


            }
        });


        if (profile.getPosition() != null) {
            CameraPosition cameraPosition
                    = new CameraPosition.Builder()
                    .target(profile.getPosition().toLatLng())
                    .zoom(13)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.floatingButton).setVisibility(View.GONE);
    }


    public void createMarker(Profile profile, Noxbox noxbox) {
        if (markers.get(noxbox.getId()) == null) {
            markers.put(noxbox.getId(), MarkerCreator.createCustomMarker(noxbox, googleMap, activity, noxbox.getOwner().getTravelMode()));
        }
    }

    public void removeMarker(String key) {
        Marker marker = markers.remove(key);
        if (marker != null) {
            marker.remove();
        }
    }

    @Override
    public boolean onClusterClick(Cluster<NoxboxMarker> cluster) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                cluster.getPosition(), googleMap.getCameraPosition().zoom + 3f));
        DebugMessage.popup(activity, "CLUSTER");

        return true;
    }

    @Override
    public boolean onClusterItemClick(final NoxboxMarker noxboxMarker) {
        DebugMessage.popup(activity, "ITEM");
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.setViewed(noxboxMarker.getNoxbox());
                profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                activity.startActivity(new Intent(activity, DetailedActivity.class));
            }
        });
        return true;
    }


    private class CustomClusterRenderer extends DefaultClusterRenderer<NoxboxMarker> {
        private final IconGenerator mClusterIconGenerator = new IconGenerator(activity.getApplicationContext());

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<NoxboxMarker> clusterManager) {
            super(context, map, clusterManager);

        }

        @Override
        protected void onBeforeClusterItemRendered(NoxboxMarker item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getMarker().getIcon());

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<NoxboxMarker> cluster, MarkerOptions markerOptions) {

            int size = 70;
            if (cluster.getSize() >= 100) {
                size += 50;
            } else if (cluster.getSize() >= 50) {
                size += 30;
            } else if (cluster.getSize() >= 20) {
                size += 20;
            } else if (cluster.getSize() >= 10) {
                size += 5;
            }

            String clusterSize = String.valueOf(cluster.getSize());

            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.noxbox), size, size, true);

            Paint paint = new Paint();
            paint.setColor(activity.getResources().getColor(R.color.secondary));
            paint.setTextSize(size / 2);


            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);
            int xPos = (int) (canvas.getWidth() - paint.measureText(clusterSize)) / 2;
            int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
            canvas.drawText(clusterSize, xPos, yPos, paint);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

        @Override
        protected void onClusterItemRendered(NoxboxMarker item, Marker marker) {
            super.onClusterItemRendered(item, marker);
            marker.setTag(item.getNoxbox());

        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<NoxboxMarker> cluster) {
            return cluster.getSize() > 1;
        }


    }

    public class NoxboxMarker implements ClusterItem {
        private LatLng position;
        private MarkerOptions markerOptions;
        private Noxbox noxbox;

        public NoxboxMarker(LatLng position, Noxbox noxbox) {
            this.position = position;
            this.noxbox = noxbox;
            this.markerOptions = MarkerCreator.createCustomMarker(noxbox, activity);

        }

        public Noxbox getNoxbox() {
            return noxbox;
        }

        public MarkerOptions getMarker() {
            return markerOptions;
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
    }

}
