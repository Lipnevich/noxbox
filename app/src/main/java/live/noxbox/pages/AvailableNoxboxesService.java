package live.noxbox.pages;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.Firebase;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static live.noxbox.state.Firebase.stopListenAvailableNoxboxes;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;

public class AvailableNoxboxesService extends Service implements ClusterManager.OnClusterClickListener<AvailableNoxboxesService.NoxboxMarker>,
        ClusterManager.OnClusterItemClickListener<AvailableNoxboxesService.NoxboxMarker> {
    private final IBinder binder = new LocalBinder();
    private Map<String, NoxboxMarker> markers = new HashMap<>();
    private ClusterManager<NoxboxMarker> clusterManager;
    private GoogleMap googleMap;

    public AvailableNoxboxesService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        String jsonMyObject = null;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("googlemap");
        }
        googleMap = new Gson().fromJson(jsonMyObject, GoogleMap.class);

        this.clusterManager = new ClusterManager<NoxboxMarker>(getApplicationContext(), googleMap);
        clusterManager.setRenderer(new CustomClusterRenderer(getApplicationContext(), googleMap, clusterManager));
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        startListenAvailableNoxboxes(googleMap);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                startListenAvailableNoxboxes(googleMap);
            }
        });

        return binder;
    }

    @Override
    public boolean onClusterClick(Cluster<NoxboxMarker> cluster) {
        DebugMessage.popup(getApplicationContext(), "CLUSTER");

        float newZoom = googleMap.getCameraPosition().zoom + 1f;
        if (newZoom < googleMap.getMaxZoomLevel())
            googleMap.animateCamera(newLatLngZoom(cluster.getPosition(), newZoom));

        // TODO (vl) показать список со всеми элементами кластера
        return true;
    }

    @Override
    public boolean onClusterItemClick(final NoxboxMarker noxboxMarker) {
        DebugMessage.popup(getApplicationContext(), "ITEM");
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                noxboxMarker.getNoxbox().getOwner().setPosition(noxboxMarker.getNoxbox().getPosition());
                profile.setViewed(noxboxMarker.getNoxbox());
                if (googleMap.getCameraPosition() != null) {
                    profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                }
                profile.getViewed().setParty(profile.notPublicInfo());
                Router.startActivity(getApplicationContext(), DetailedActivity.class);
            }
        });
        return true;
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

    private void startListenAvailableNoxboxes(GoogleMap googleMap) {
        Firebase.startListenAvailableNoxboxes(MapActivity.getCameraPosition(googleMap).toGeoLocation(), moved, removed);

    }

    @Override
    public void unbindService(ServiceConnection conn) {
        stopListenAvailableNoxboxes();
        clusterManager.clearItems();
        super.unbindService(conn);
    }

    @Override
    public void onDestroy() {
        stopListenAvailableNoxboxes();
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        AvailableNoxboxesService getService() {
            return AvailableNoxboxesService.this;
        }
    }

    private class CustomClusterRenderer extends DefaultClusterRenderer<NoxboxMarker> {

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

            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.noxbox), size, size, true);

            Paint paint = new Paint();
            paint.setColor(getApplicationContext().getResources().getColor(R.color.secondary));
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

    private AtomicInteger noxboxCount = new AtomicInteger();
    private AtomicLong totalTime = new AtomicLong();
    private String TAG = this.getClass().getName();

    private void createMarker(final Noxbox noxbox) {
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                int count = noxboxCount.incrementAndGet();
                long currentTimeInMillis = System.currentTimeMillis();
                if (count % 100 == 0) {
                    Log.d(TAG, "Average creation time " + String.valueOf(totalTime.intValue() / count));
                }

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


                NoxboxMarker noxboxMarker = new NoxboxMarker(noxbox.getPosition().toLatLng(), noxbox, getApplicationContext());

                markers.put(noxbox.getId(), noxboxMarker);
                clusterManager.addItem(noxboxMarker);
                clusterManager.cluster();

                totalTime.addAndGet(System.currentTimeMillis() - currentTimeInMillis);
            }
        });
    }
}
