package live.noxbox.pages;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;

import live.noxbox.BuildConfig;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.constructor.NoxboxTypeListActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.MapController;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Task;

import static live.noxbox.state.Firebase.stopListenAvailableNoxboxes;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.Router.startActivityForResult;

public class AvailableServices implements State {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;



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


    }

    @Override
    public void draw(final Profile profile) {
        //MapController.moveCopyrightRight(googleMap);
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

        Intent intent = new Intent(activity, AvailableNoxboxesService.class);
        intent.putExtra("googlemap", new Gson().toJson(googleMap));
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void clear() {
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
        if (mBound) {
            stopListenAvailableNoxboxes();
            activity.unbindService(mConnection);
            mBound = false;
        }

    }

    private AvailableNoxboxesService availableNoxboxesService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AvailableNoxboxesService.LocalBinder binder = (AvailableNoxboxesService.LocalBinder) service;
            availableNoxboxesService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}



