package live.noxbox.states;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.Constants;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.activities.contract.NoxboxTypeListFragment;
import live.noxbox.cluster.ClusterManager;
import live.noxbox.database.AppCache;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.services.AvailableNoxboxesService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.SeparateStreamForStopwatch;
import live.noxbox.tools.Task;
import live.noxbox.ui.RoleSwitcherLayout;

import static live.noxbox.activities.contract.NoxboxTypeListFragment.MAP_CODE;
import static live.noxbox.database.AppCache.availableNoxboxes;
import static live.noxbox.database.GeoRealtime.startListenAvailableNoxboxes;
import static live.noxbox.database.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.LocationOperator.getLocationPermission;
import static live.noxbox.tools.MapOperator.getCameraPosition;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;

public class AvailableNoxboxes implements State {

    private GoogleMap googleMap;
    private MapActivity activity;
    private Profile profile = AppCache.profile();

    private ClusterManager clusterManager;

    private Handler serviceHandler;
    private Runnable serviceRunnable;

    private static boolean serviceIsBound = false;

    public static volatile int clusterRenderingFrequency = 400;
    private DialogFragment dialog;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        startListenAvailableNoxboxes(getCameraPosition(googleMap).toGeoLocation(), availableNoxboxes);
        if (clusterManager == null) {
            clusterManager = new ClusterManager(activity, googleMap);
        }
        googleMap.setOnMarkerClickListener(clusterManager.getRenderer());
        googleMap.setOnCameraIdleListener(() -> startListenAvailableNoxboxes(getCameraPosition(googleMap).toGeoLocation(), availableNoxboxes));
        MapOperator.moveCopyrightRight(googleMap);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pointerImage).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.filter).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.switcherLayout).setVisibility(View.VISIBLE);
        ((RoleSwitcherLayout) activity.findViewById(R.id.switcherLayout)
                .findViewById(R.id.realSwitcherLayout)).refresh();

        activity.findViewById(R.id.locationButton).setOnClickListener(v -> {
            getLocationPermission(activity, Constants.LOCATION_PERMISSION_REQUEST_CODE);
            getDeviceLocation(profile, googleMap, activity);
        });

        activity.findViewById(R.id.filter).setOnClickListener(v -> {
            if (dialog == null || !dialog.isVisible()) {
                dialog = new NoxboxTypeListFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("key", MAP_CODE);
                dialog.setArguments(bundle);
                dialog.show((activity).getSupportFragmentManager(), NoxboxTypeListFragment.TAG);
            }
        });

        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> {
            profile.setNoxboxId("");

            profile.getCurrent().create(Position.from(googleMap.getCameraPosition().target), profile.publicInfo());

            startActivity(activity, ContractActivity.class);
        });

        if (!serviceIsBound) {
            serviceHandler = new Handler();
            serviceRunnable = () -> {
                final Intent intent = new Intent(activity, AvailableNoxboxesService.class);
                activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            };
            serviceHandler.post(serviceRunnable);
        }
    }

    @Override
    public void clear() {
        //service clear
        if (serviceIsBound) {
            stopHandler();
            mConnection.onServiceDisconnected(new ComponentName(activity.getApplicationContext().getPackageName(), AvailableNoxboxes.class.getName()));
            try {
                activity.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                // ignore this
            }
            serviceIsBound = false;
        }
        if (serviceHandler != null) {
            serviceHandler.removeCallbacksAndMessages(null);
        }

        //map clear
        googleMap.clear();
        googleMap.setOnCameraIdleListener(() -> {
        });
        googleMap.setOnMarkerClickListener(marker -> true);
        activity.findViewById(R.id.pointerImage).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.filter).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        MapOperator.moveCopyrightLeft(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.switcherLayout).setVisibility(View.GONE);
        stopListenAvailableNoxboxes();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AvailableNoxboxesService.LocalBinder binder = (AvailableNoxboxesService.LocalBinder) service;
            // AvailableNoxboxesService availableNoxboxesService = binder.getService();
            serviceIsBound = true;

            Log.d("AvailableNoxboxes", "onServiceConnected()");

            AppCache.readProfile(profile -> {
                Task task = object -> clusterManager.setItems(availableNoxboxes, profile);

                SeparateStreamForStopwatch.startHandler(task, clusterRenderingFrequency);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("AvailableNoxboxes", "onServiceDisconnected()");
            SeparateStreamForStopwatch.stopHandler();
            serviceIsBound = false;
        }
    };


}

