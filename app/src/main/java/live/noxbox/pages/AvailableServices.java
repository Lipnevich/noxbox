package live.noxbox.pages;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.constructor.NoxboxTypeListActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.state.GeoRealtime;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.state.cluster.Callbacks;
import live.noxbox.state.cluster.Cluster;
import live.noxbox.state.cluster.ClusterManager;
import live.noxbox.state.cluster.NoxboxMarker;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static live.noxbox.state.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.Router.startActivityForResult;

public class AvailableServices implements State {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private static Map<String, Noxbox> markers = new ConcurrentHashMap<>();

    private ClusterManager clusterManager;

    private Handler serviceHandler;
    private Runnable serviceRunnable;

    public AvailableServices(final GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;

        createClusterManager();

        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });
    }

    private void createClusterManager() {
        clusterManager = new ClusterManager(activity.getApplicationContext(), googleMap)
                .setCallbacks(new Callbacks() {
                    @Override
                    public boolean onClusterClick(@NonNull Cluster<NoxboxMarker> cluster) {
                        DebugMessage.popup(activity, "CLUSTER");

                        float newZoom = googleMap.getCameraPosition().zoom + 1f;
                        if (newZoom < googleMap.getMaxZoomLevel()) {
                            LatLng center = MapController.getCenterBetweenSomeLocations(cluster.getItems());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, newZoom));
                        } else {
                            // TODO (vl) показать список со всеми элементами кластера
                        }
                        return false;
                    }

                    @Override
                    public boolean onClusterItemClick(@NonNull final NoxboxMarker clusterItem) {
                        DebugMessage.popup(activity, "ITEM");
                        ProfileStorage.readProfile(new Task<Profile>() {
                            @Override
                            public void execute(Profile profile) {
                                profile.setViewed(clusterItem.getNoxbox());
                                if (googleMap.getCameraPosition() != null) {
                                    profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                                }
                                profile.getViewed().setParty(profile.notPublicInfo());
                                Router.startActivity(activity, DetailedActivity.class);
                            }
                        });
                        return false;
                    }
                });
    }


    private static boolean serviceIsBound = false;

    @Override
    public void draw(final Profile profile) {
        startListenAvailableNoxboxes();
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                startListenAvailableNoxboxes();
            }
        });
        MapController.moveCopyrightRight(googleMap);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pointerImage).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.filter).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);


        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });

        activity.findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, NoxboxTypeListActivity.class);
                intent.putExtra(MapActivity.class.getName(), NoxboxTypeListActivity.class.getName());
                startActivityForResult(activity, intent, NoxboxTypeListActivity.MAP_CODE);
            }
        });

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

        if (!serviceIsBound) {
            serviceHandler = new Handler();
            serviceRunnable = new Runnable() {
                @Override
                public void run() {
                    final Intent intent = new Intent(activity, AvailableNoxboxesService.class);
                    activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                }
            };
            serviceHandler.post(serviceRunnable);
        }
    }

    @Override
    public void clear() {
        //service clear
        if (serviceIsBound) {
            if (drawingHeandler != null) {
                drawingHeandler.removeCallbacksAndMessages(drawingRunnable);
                drawingHeandler.removeCallbacks(drawingRunnable);
            }
            mConnection.onServiceDisconnected(new ComponentName(activity.getApplicationContext().getPackageName(), AvailableServices.class.getName()));
            activity.unbindService(mConnection);
            serviceIsBound = false;
        }
        if (serviceHandler != null) {
            serviceHandler.removeCallbacksAndMessages(null);
        }
        //map clear
        googleMap.clear();
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
        activity.findViewById(R.id.pointerImage).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.filter).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        MapController.moveCopyrightLeft(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        stopListenAvailableNoxboxes();
    }

    private Handler drawingHeandler = new Handler();
    private Runnable drawingRunnable;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AvailableNoxboxesService.LocalBinder binder = (AvailableNoxboxesService.LocalBinder) service;
            AvailableNoxboxesService availableNoxboxesService = binder.getService();
            serviceIsBound = true;

            Log.d(TAG + "AvailableServices", "onServiceConnected()");

            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    drawingRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG + "AvailableServices", "run()");
                            clusterManager.setItems(markers, profile);
                            drawingHeandler.postDelayed(drawingRunnable, 200);
                        }
                    };

                    drawingHeandler.post(drawingRunnable);
                }
            });


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG + "AvailableServices", "onServiceDisconnected()");
            if (drawingHeandler != null) {
                drawingHeandler.removeCallbacksAndMessages(drawingRunnable);
            }
            serviceIsBound = false;
        }
    };

    private void startListenAvailableNoxboxes() {
        GeoRealtime.startListenAvailableNoxboxes(MapActivity.getCameraPosition(googleMap).toGeoLocation(), markers);

    }


}

