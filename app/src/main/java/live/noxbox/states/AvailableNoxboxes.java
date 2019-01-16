package live.noxbox.states;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.activities.contract.NoxboxTypeListFragment;
import live.noxbox.cluster.ClusterManager;
import live.noxbox.database.AppCache;
import live.noxbox.debug.GMailSender;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.services.AvailableNoxboxesService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.SeparateStreamForStopwatch;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.MapActivity.getCameraPosition;
import static live.noxbox.activities.contract.NoxboxTypeListFragment.MAP_CODE;
import static live.noxbox.database.AppCache.availableNoxboxes;
import static live.noxbox.database.GeoRealtime.startListenAvailableNoxboxes;
import static live.noxbox.database.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;

public class AvailableNoxboxes implements State {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    public static volatile int clusterRenderingFrequency = 400;

    private ClusterManager clusterManager;

    private Handler serviceHandler;
    private Runnable serviceRunnable;

    private static boolean serviceIsBound = false;

    public AvailableNoxboxes(final GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;
        MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null) return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void draw(final Profile profile) {
        startListenAvailableNoxboxes(getCameraPosition(googleMap).toGeoLocation(), availableNoxboxes);
        if (clusterManager == null) {
            clusterManager = new ClusterManager(activity, googleMap);
        }
        googleMap.setOnMarkerClickListener(clusterManager.getRenderer());
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                startListenAvailableNoxboxes(getCameraPosition(googleMap).toGeoLocation(), availableNoxboxes);
            }
        });
        MapOperator.moveCopyrightRight(googleMap);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pointerImage).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.filter).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);

        activity.findViewById(R.id.pointerImage).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String deviceModel = android.os.Build.MODEL;
                String deviceBrand = Build.BRAND;
                String deviceBootloader = Build.BOOTLOADER;
                String deviceOverallNameProduct = Build.PRODUCT;
                String hasGpsDevice = String.valueOf(hasGPSDevice(activity));

//
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("message/rfc822");
//                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"vladviva5991@gmail.com"});
//                i.putExtra(Intent.EXTRA_SUBJECT, "systemInfo");
//                i.putExtra(Intent.EXTRA_TEXT, "Model: " + deviceModel + " | " + "Brand: " + deviceBrand + " | " + "Bootloader: " + deviceBootloader + " | " + "OverallNameProduct: " + deviceOverallNameProduct + " | " + "hasGpsDevice: " + hasGpsDevice);
//                try {
//                    activity.startActivity(Intent.createChooser(i, "Send mail..."));
//                    Toast.makeText(activity, "Информация об ошибке отправлена", Toast.LENGTH_LONG).show();
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//                }
                String mail = "Model: " + deviceModel + " | " + "Brand: " + deviceBrand + " | " + "Bootloader: " + deviceBootloader + " | " + "OverallNameProduct: " + deviceOverallNameProduct + " | " + "hasGpsDevice: " + hasGpsDevice;

                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            GMailSender sender = new GMailSender("testnoxbox2018@gmail.com", "noxboxtest");
                            sender.sendMail("This is Subject",
                                    mail,
                                    "testnoxbox2018@gmail.com",
                                    "vladviva5991@gmail.com");
                        } catch (Exception e) {
                            Log.e("GMailSender.class", e.getMessage(), e);
                        }
                        return null;
                    }
                }.execute();


                Toast.makeText(activity, "Информация об ошибке отправлена", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MapActivity.isLocationPermissionGranted(activity)) {
                    MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        });

        activity.findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) при повторном выборе услуги в фильтрах не происходит перерисовка услуг соответствующих выбранной
                DialogFragment dialog = new NoxboxTypeListFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("key", MAP_CODE);
                dialog.setArguments(bundle);
                dialog.show(((FragmentActivity) activity).getSupportFragmentManager(), NoxboxTypeListFragment.TAG);
            }
        });

        activity.findViewById(R.id.customFloatingView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setNoxboxId(null);
                profile.getCurrent().clean();
                profile.getCurrent().setPosition(Position.from(googleMap.getCameraPosition().target));
                profile.getCurrent().setOwner(profile.publicInfo());
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    profile.getCurrent().getOwner().setPosition(getCameraPosition(googleMap));
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
        MapOperator.moveCopyrightLeft(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        stopListenAvailableNoxboxes();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AvailableNoxboxesService.LocalBinder binder = (AvailableNoxboxesService.LocalBinder) service;
            // AvailableNoxboxesService availableNoxboxesService = binder.getService();
            serviceIsBound = true;

            Log.d("AvailableNoxboxes", "onServiceConnected()");

            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    Task task = new Task() {
                        @Override
                        public void execute(Object object) {
                            clusterManager.setItems(availableNoxboxes, profile);
                        }
                    };

                    SeparateStreamForStopwatch.startHandler(task, clusterRenderingFrequency);
                }
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

