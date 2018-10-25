package live.noxbox.pages;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.BuildConfig;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.constructor.NoxboxTypeListActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.GeoRealtime;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static live.noxbox.Configuration.MIN_ZOOM_LEVEL_FOR_SHOWING_MARKER_IN_VISIBLE_AREA;
import static live.noxbox.state.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.Router.startActivityForResult;

public class AvailableServices implements State, ClusterManager.OnClusterClickListener<AvailableServices.NoxboxMarker>,
        ClusterManager.OnClusterItemClickListener<AvailableServices.NoxboxMarker> {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private Map<String, NoxboxMarker> markers = new ConcurrentHashMap<>();
    private ClusterManager<NoxboxMarker> clusterManager;
    private Handler serviceHandler;
    private Runnable serviceRunnable;


    public AvailableServices(final GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;

        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });
        clusterManager = new ClusterManager<NoxboxMarker>(activity, googleMap);

        clusterManager.setRenderer(new CustomClusterRenderer(activity, googleMap, clusterManager));
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

    }


    private static Boolean isNotBind = true;

    @Override
    public void draw(final Profile profile) {
        MapController.moveCopyrightRight(googleMap);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pointerImage).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
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


        if (BuildConfig.DEBUG) {
            activity.findViewById(R.id.debugGenerateNoxboxes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (Noxbox noxbox : NoxboxExamples.generateNoxboxes(MapActivity.getCameraPosition(googleMap), 50, profile)) {
                        //online(noxbox);
                    }
                }
            });
        }

        if (isNotBind) {
            serviceHandler = new Handler();
            serviceRunnable = new Runnable() {
                @Override
                public void run() {
                    final Intent intent = new Intent(activity, AvailableNoxboxesService.class);
                    activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                }
            };
            serviceHandler.post(serviceRunnable);

            isNotBind = false;
        }
    }

    @Override
    public void clear() {
        if (serviceHandler != null) {
            serviceHandler.removeCallbacksAndMessages(null);
        }
        clusterManager.clearItems();
        googleMap.clear();
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
            }
        });
        activity.findViewById(R.id.pointerImage).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.filter).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        MapController.moveCopyrightLeft(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        if (serviceIsBound) {
            stopListenAvailableNoxboxes();
            activity.unbindService(mConnection);
            serviceIsBound = false;
        }
        markers.clear();
    }

    private boolean serviceIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AvailableNoxboxesService.LocalBinder binder = (AvailableNoxboxesService.LocalBinder) service;
            AvailableNoxboxesService availableNoxboxesService = binder.getService();
            serviceIsBound = true;

            startListenAvailableNoxboxes(googleMap);
            googleMap.setOnCameraMoveListener(
                    new GoogleMap.OnCameraMoveListener() {
                        @Override
                        public void onCameraMove() {
                            startListenAvailableNoxboxes(googleMap);
                        }
                    }
            );


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceIsBound = false;
        }
    };

    @Override
    public boolean onClusterClick(Cluster<NoxboxMarker> cluster) {
        DebugMessage.popup(activity, "CLUSTER");

        float newZoom = googleMap.getCameraPosition().zoom + 1f;
        if (newZoom < googleMap.getMaxZoomLevel())
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
                Router.startActivity(activity, DetailedActivity.class);
            }
        });
        return true;
    }

    private Task moved = new Task<Noxbox>() {
        @Override
        public void execute(final Noxbox noxbox) {
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

    private void startListenAvailableNoxboxes(GoogleMap googleMap) {
        GeoRealtime.startListenAvailableNoxboxes(MapActivity.getCameraPosition(googleMap).toGeoLocation(), moved, removed);

    }

    private class CustomClusterRenderer extends DefaultClusterRenderer<NoxboxMarker> {

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<NoxboxMarker> clusterManager) {
            super(context, map, clusterManager);

        }

        @Override
        protected void onBeforeClusterItemRendered(NoxboxMarker item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getMarker().getIcon());

        }

        private Map<Integer, Bitmap> bitmapCaches = new ConcurrentHashMap<>();

        private Bitmap getBitmap(int size) {
            if (bitmapCaches.containsKey(size)) {
                return bitmapCaches.get(size);
            }
            bitmapCaches.put(size, Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.noxbox), size, size, true));
            return bitmapCaches.get(size);
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

            Bitmap bitmap = getBitmap(size).copy(getBitmap(size).getConfig(), true);

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

        public NoxboxMarker(LatLng position, final Noxbox noxbox, Context context) {
            this.position = position;
            this.noxbox = noxbox;
            this.markerOptions = new MarkerOptions();
            //TODO (vl) try to use custom caches (not glide)
            Glide.with(context)
                    .asBitmap()
                    .load(noxbox.getType().getImage())
                    .apply(RequestOptions.overrideOf(dpToPx(56), dpToPx(56)))
                    .apply(RequestOptions.fitCenterTransform())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                            markerOptions.position(noxbox.getPosition().toLatLng())
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .anchor(0.5f, 1f);
                        }
                    });


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

    private void removeMarker(Noxbox noxbox) {
        clusterManager.removeItem(markers.get(noxbox.getId()));
        markers.remove(noxbox.getId());
        clusterManager.cluster();
    }

    private void createMarker(final Noxbox noxbox) {
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (profile.getDarkList().get(noxbox.getOwner().getId()) != null) return;

                if (!profile.getFilters().getTypes().get(noxbox.getType().name())) return;

                if (Integer.parseInt(noxbox.getPrice()) > Integer.parseInt(profile.getFilters().getPrice()))
                    return;

                if (noxbox.getRole() == MarketRole.demand && !profile.getFilters().getDemand())
                    return;

                if (noxbox.getRole() == MarketRole.supply && !profile.getFilters().getSupply())
                    return;

                //фильтры по типу передвижения
                if (noxbox.getOwner().getHost() && noxbox.getOwner().getTravelMode() == TravelMode.none) {
                    if (profile.getTravelMode() == TravelMode.none) return;
                }

                if (!noxbox.getOwner().getHost()) {
                    if (!profile.getHost()) return;
                }

                NoxboxMarker noxboxMarker = new NoxboxMarker(noxbox.getPosition().toLatLng(), noxbox, activity);

                markers.put(noxbox.getId(), noxboxMarker);

                clusterManager.addItem(noxboxMarker);
                clusterManager.cluster();

            }
        });
    }

    private LatLngBounds getVisibleArea(GoogleMap googleMap){
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
        return visibleRegion.latLngBounds;
    }

    private Boolean isInVisibleArea(LatLng position, LatLngBounds latLngBounds) {
        return (latLngBounds == null ? googleMap.getProjection().getVisibleRegion().latLngBounds : latLngBounds).contains(position);
    }

    private Boolean isMinimalZoomLevel() {
        return googleMap.getCameraPosition().zoom > MIN_ZOOM_LEVEL_FOR_SHOWING_MARKER_IN_VISIBLE_AREA;
    }

}



