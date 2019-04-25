package live.noxbox.states;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import live.noxbox.Constants;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.activities.contract.NoxboxTypeListAdapter;
import live.noxbox.cluster.ClusterManager;
import live.noxbox.database.AppCache;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.services.AvailableNoxboxesService;
import live.noxbox.states.decorator.StatesDecorator;
import live.noxbox.tools.SeparateStreamForStopwatch;
import live.noxbox.tools.Task;
import live.noxbox.tools.location.LocationUpdater;
import live.noxbox.ui.RoleSwitcherLayout;

import static live.noxbox.activities.contract.NoxboxTypeListAdapter.MAP_CODE;
import static live.noxbox.database.AppCache.availableNoxboxes;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.isProfileReady;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.GeoRealtime.startListenAvailableNoxboxes;
import static live.noxbox.database.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.MapOperator.getCameraPosition;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;
import static live.noxbox.tools.location.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;
import static live.noxbox.tools.location.LocationUpdater.KEY_REQUESTING_LOCATION_UPDATES;

public class AvailableNoxboxes implements State {

    private static GoogleMap googleMap;
    private MapActivity activity;
    private Profile profile = AppCache.profile();

    private ClusterManager clusterManager;

    private Handler serviceHandler;
    private Runnable serviceRunnable;

    private static boolean serviceIsBound = false;

    public static volatile int clusterRenderingFrequency = 400;
    private static DialogFragment noxboxTypeListFragment;

    private LocationUpdater locationUpdater;

    private StatesDecorator decorator;

    public AvailableNoxboxes(StatesDecorator decorator) {
        this.decorator = decorator;
    }

    public AvailableNoxboxes() {
    }

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        AvailableNoxboxes.googleMap = googleMap;
        this.activity = activity;
        if (locationUpdater == null && isLocationPermissionGranted(activity)) {
            locationUpdater = new LocationUpdater(activity);
            locationUpdater.startLocationUpdates();
        }


        startListenAvailableNoxboxes(getCameraPosition(googleMap).toGeoLocation(), availableNoxboxes, null);
        if (clusterManager == null) {
            clusterManager = new ClusterManager(activity, googleMap);
        }
        googleMap.setOnMarkerClickListener(clusterManager.getRenderer());
        googleMap.setOnCameraIdleListener(() -> startListenAvailableNoxboxes(getCameraPosition(googleMap).toGeoLocation(), availableNoxboxes, null));
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pointerImage).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.filter).setVisibility(View.VISIBLE);
        if (isProfileReady()) {
            activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
        }
        activity.findViewById(R.id.switcherLayout).setVisibility(View.VISIBLE);
        ((RoleSwitcherLayout) activity.findViewById(R.id.switcherLayout)
                .findViewById(R.id.roleSwitcherLayout)).refresh();

        activity.findViewById(R.id.locationButton).setOnClickListener(v -> {
            updateDeviceLocation(profile, activity);
        });

        activity.findViewById(R.id.filter).setOnClickListener(v -> {
            createCommonFragmentOfNoxboxTypeList(activity, MAP_CODE);
        });

        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> {
            createContract(profile, activity);
        });

        if (!serviceIsBound) {
            serviceHandler = new Handler();
            serviceRunnable = () -> {
                final Intent intent = new Intent(activity, AvailableNoxboxesService.class);
                activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            };
            serviceHandler.post(serviceRunnable);
        }

        if (decorator != null) {
            decorator.draw(googleMap, activity);
        }
    }

    public void onSaveRequestingLocationUpdatesState(Bundle savedInstanceState) {
        if (locationUpdater != null) {
            savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, locationUpdater.isRequestingLocationUpdates());
        }
    }

    public void updateRequestingLocationUpdatesFromBundle(Boolean requestingLocationUpdates) {
        if (locationUpdater != null && !locationUpdater.isRequestingLocationUpdates()) {
            locationUpdater.setRequestingLocationUpdates(requestingLocationUpdates);
        }
    }

    @Override
    public void clear() {
        if (decorator != null) {
            decorator.clear();
            decorator = null;
        }
        if (locationUpdater != null) {
            locationUpdater.stopLocationUpdates();
            locationUpdater = null;
        }
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
        if (clusterManager != null) {
            clusterManager.clear();
        }
        clusterManager = null;


        googleMap.clear();
        googleMap.setOnCameraIdleListener(() -> {
        });
        googleMap.setOnMarkerClickListener(marker -> true);
        if (noxboxTypeListFragment != null) {
            noxboxTypeListFragment.dismiss();
            noxboxTypeListFragment = null;
        }
        activity.findViewById(R.id.pointerImage).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.filter).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.switcherLayout).setVisibility(View.GONE);
        stopListenAvailableNoxboxes();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceIsBound = true;
            Task update = with -> {
                if (clusterManager != null) {
                    clusterManager.setItems(availableNoxboxes, profile);
                }
            };
            SeparateStreamForStopwatch.startHandler(update, clusterRenderingFrequency);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            SeparateStreamForStopwatch.stopHandler();
            serviceIsBound = false;
        }
    };

    public StatesDecorator getDecorator() {
        return decorator;
    }

    public static void createContract(Profile profile, Activity activity) {
        if (googleMap == null) return;

        profile.setNoxboxId("");

        profile.getCurrent().create(Position.from(googleMap.getCameraPosition().target), profile.publicInfo(profile.getCurrent().getRole(), profile.getCurrent().getType()));
        startActivity(activity, ContractActivity.class);
    }

    public static void updateDeviceLocation(Profile profile, Activity activity) {
        if (googleMap == null) return;
        startLocationPermissionRequest(activity, Constants.LOCATION_PERMISSION_REQUEST_CODE);
        getDeviceLocation(profile, googleMap, activity);
    }

    public static void createCommonFragmentOfNoxboxTypeList(FragmentActivity activity, int key) {
        activity.findViewById(R.id.noxboxTypeListLayout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.noxboxTypeListLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.findViewById(R.id.noxboxTypeListLayout).setVisibility(View.GONE);
            }
        });

        if (key == MAP_CODE) {
            activity.findViewById(R.id.itemLayout).setVisibility(View.VISIBLE);
            ((ImageView) activity.findViewById(R.id.itemLayout).findViewById(R.id.noxboxTypeImage)).setImageResource(R.drawable.noxbox);
            ((TextView) activity.findViewById(R.id.itemLayout).findViewById(R.id.noxboxTypeName)).setText(R.string.showAll);
            activity.findViewById(R.id.itemLayout).setOnClickListener(v -> executeAllInTheMap(activity));
        } else {
            activity.findViewById(R.id.itemLayout).setVisibility(View.GONE);
        }

        List<NoxboxType> noxboxTypes = new ArrayList<>(Arrays.asList(NoxboxType.values()));
        RecyclerView noxboxTypeList = activity.findViewById(R.id.listOfServices);
        noxboxTypeList.setHasFixedSize(true);
        noxboxTypeList.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        noxboxTypeList.setAdapter(new NoxboxTypeListAdapter(noxboxTypes, activity, key));

    }

    private static void executeAllInTheMap(Activity activity) {
        for (NoxboxType type : NoxboxType.values()) {
            profile().getFilters().getTypes().put(type.name(), true);
        }
        activity.findViewById(R.id.noxboxTypeListLayout).setVisibility(View.GONE);
        executeUITasks();
    }
}

