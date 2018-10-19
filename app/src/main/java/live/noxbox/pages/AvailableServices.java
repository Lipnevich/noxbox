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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.Map;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.constructor.NoxboxTypeListActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.state.Firebase;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.Task;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static live.noxbox.state.Firebase.stopListenAvailableNoxboxes;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.Router.startActivityForResult;

public class AvailableServices implements State, ClusterManager.OnClusterClickListener<AvailableServices.NoxboxMarker>,
        ClusterManager.OnClusterItemClickListener<AvailableServices.NoxboxMarker> {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private Map<String, NoxboxMarker> markers = new HashMap<>();
    private ClusterManager<NoxboxMarker> clusterManager;
    private CustomClusterRenderer customClusterRenderer;


    public AvailableServices(final GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;
        this.clusterManager = new ClusterManager<>(activity, googleMap);

        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.setOnCameraIdleListener(clusterManager);

        customClusterRenderer = new CustomClusterRenderer(activity, googleMap, clusterManager);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);

        clusterManager.setRenderer(customClusterRenderer);

        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (profile.getPosition() != null) {
                    MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
                }
            }
        });

    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        MapController.moveCopyrightRight(googleMap);
        activity.findViewById(R.id.pointerImage).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        startListenAvailableNoxboxes();
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                startListenAvailableNoxboxes();
            }
        });

        activity.findViewById(R.id.filter).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, NoxboxTypeListActivity.class);
                intent.putExtra(MapActivity.class.getName(), NoxboxTypeListActivity.class.getName());
                startActivityForResult(activity, intent, NoxboxTypeListActivity.MAP_CODE);
            }
        });

        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setPosition(Position.from(googleMap.getCameraPosition().target));
                profile.getCurrent().setOwner(profile.publicInfo());
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    profile.getCurrent().getOwner().setPosition(Position.from(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                }

                startActivity(activity, ConstructorActivity.class);

            }
        });
    }

    @Override
    public void clear() {
        stopListenAvailableNoxboxes();
        googleMap.clear();
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
            }
        });
        clusterManager.clearItems();
        activity.findViewById(R.id.pointerImage).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.filter).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        MapController.moveCopyrightLeft(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);

    }


    private void createMarker(final Noxbox noxbox) {
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if(!profile.getFilters().getTypes().get(noxbox.getType().name())) return;
                // TODO (vl) проверить другие фильтры, время работы, черный список, и совместимость по типу передвижения
                NoxboxMarker noxboxMarker = new NoxboxMarker(noxbox.getPosition().toLatLng(), noxbox);
                markers.put(noxbox.getId(), noxboxMarker);
                clusterManager.addItem(noxboxMarker);
                clusterManager.cluster();
            }
        });


    }

    private void removeMarker(Noxbox noxbox) {
        clusterManager.removeItem(markers.get(noxbox.getId()));
        markers.remove(noxbox.getId());
        clusterManager.cluster();
    }

    @Override
    public boolean onClusterClick(Cluster<NoxboxMarker> cluster) {
        DebugMessage.popup(activity, "CLUSTER");

        float newZoom = googleMap.getCameraPosition().zoom + 1f;
        if(newZoom < googleMap.getMaxZoomLevel())
            googleMap.animateCamera(newLatLngZoom(cluster.getPosition(), newZoom));

        // TODO (vl) показать список со всеми элементами кластера
        return true;
    }

    @Override
    public boolean onClusterItemClick(final NoxboxMarker noxboxMarker) {
        DebugMessage.popup(activity, "ITEM");
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                noxboxMarker.getNoxbox().getOwner().setPosition(noxboxMarker.getNoxbox().getPosition());
                profile.setViewed(noxboxMarker.getNoxbox());
                if (googleMap.getCameraPosition() != null) {
                    profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                }
                profile.getViewed().setParty(profile.notPublicInfo());
                startActivity(activity, DetailedActivity.class);
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

            int size = 85;
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
            this.markerOptions = MarkerCreator.createCustomMarker(noxbox, activity.getResources());

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

    private Task moved = new Task<Noxbox>() {
        @Override
        public void execute(Noxbox noxbox) {
            if (markers.get(noxbox.getId()) != null) {
                markers.get(noxbox.getId()).getMarker().position(noxbox.getPosition().toLatLng());
            } else {
                createMarker(noxbox);
            }
        }
    };
    private Task removed = new Task<Noxbox>() {
        @Override
        public void execute(Noxbox noxbox) {
            removeMarker(noxbox);
        }
    };

    private void startListenAvailableNoxboxes() {
        Firebase.startListenAvailableNoxboxes(MapActivity.getCameraPosition(googleMap).toGeoLocation(), moved, removed);

    }


}
