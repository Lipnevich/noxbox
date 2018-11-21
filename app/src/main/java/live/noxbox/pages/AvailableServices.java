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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.contract.ContractActivity;
import live.noxbox.contract.NoxboxTypeListActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.state.State;
import live.noxbox.state.cluster.ClusterManager;
import live.noxbox.tools.MapController;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.CLUSTER_RENDERING_FREQUENCY;
import static live.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.database.AppCache.markers;
import static live.noxbox.database.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.Router.startActivityForResult;

public class AvailableServices implements State {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;

    private ClusterManager clusterManager;

    private Handler serviceHandler;
    private Runnable serviceRunnable;

    public AvailableServices(final GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;
        MapController.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    private static boolean serviceIsBound = false;

    @Override
    public void draw(final Profile profile) {
        startListenAvailableNoxboxes();
        if (clusterManager == null) {
            clusterManager = new ClusterManager(activity, googleMap);
        }
        googleMap.setOnMarkerClickListener(clusterManager.getRenderer());
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
                if (MapActivity.isLocationPermissionGranted(activity)) {
                    MapController.buildMapPosition(googleMap, activity.getApplicationContext());
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
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
                    profile.getCurrent().getOwner().setPosition(MapActivity.getCameraPosition(googleMap));
                }

                startActivity(activity, ContractActivity.class);

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
                return true;
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

            Log.d("AvailableServices", "onServiceConnected()");

            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    drawingRunnable = new Runnable() {
                        @Override
                        public void run() {
                            clusterManager.setItems(markers, profile);
                            drawingHeandler.postDelayed(drawingRunnable, CLUSTER_RENDERING_FREQUENCY);
                        }
                    };

                    drawingHeandler.post(drawingRunnable);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("AvailableServices", "onServiceDisconnected()");
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

