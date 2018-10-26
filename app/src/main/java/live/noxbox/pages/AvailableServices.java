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
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.BuildConfig;
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
import live.noxbox.state.cluster.ClusterManager;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;
import static live.noxbox.state.GeoRealtime.stopListenAvailableNoxboxes;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.Router.startActivityForResult;

public class AvailableServices implements State {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private static Map<String, Noxbox> markers = new ConcurrentHashMap<>();

    private ClusterManager clusterManager;

    // TODO (nli) how much time we need for one refresh?
    private Handler serviceHandler;
    private Runnable serviceRunnable;

    private static Map<String, Noxbox> markersS = new ConcurrentHashMap<>();
    public AvailableServices(final GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;

        this.clusterManager = new ClusterManager(activity.getApplicationContext(), googleMap);

        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                for(Noxbox noxbox : NoxboxExamples.generateNoxboxes(profile.getPosition(),10000,profile)){
                    markersS.put(noxbox.getId(),noxbox);
                }
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        startListenAvailableNoxboxes();

                        clusterManager.setItems(markersS, profile);
                    }
                });

                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });
//        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                List<Noxbox> noxboxes = ((List<Noxbox>)marker.getTag());
//                if(noxboxes.size() == 1) {
//                    onClusterItemClick(noxboxes.get(0));
//                } else {
//                    onClusterClick(noxboxes, marker.getPosition());
//                }
//                return true;
//            }
//        });
        startListenAvailableNoxboxes();
    }

//    private void refresh() {
//        ProfileStorage.readProfile(new Task<Profile>() {
//            @Override
//            public void execute(Profile profile) {
//                LatLngBounds screen = googleMap.getProjection().getVisibleRegion().latLngBounds;
//                int columns = 3;
//                int rows = 5;
//                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    columns = 5;
//                    rows = 3;
//                }
//                List[][] clusters = new ArrayList[columns][rows];
//                for(int x = 0; x < columns; x++) {
//                    for(int y = 0; y < rows; y++) {
//                        clusters[x][y] = new ArrayList();
//                    }
//                }
//
//                TimeLogger timeLogger = new TimeLogger();
//                double cellWidth = getWidth(screen.southwest.longitude, screen.northeast.longitude) / columns;
//                double cellHeight = getHeight(screen.southwest.latitude, screen.northeast.latitude) / rows;
//                for (Noxbox noxbox : markers.values()) {
//                    if (isFiltered(profile, noxbox, screen)) continue;
//
//                    int column = (int) Math.round(getWidth(screen.southwest.longitude, noxbox.getPosition().getLongitude()) / cellWidth);
//                    int row = (int) Math.round(getHeight(screen.southwest.latitude, noxbox.getPosition().getLatitude()) / cellHeight);
//                    if(column >= columns) {
//                        Log.d("out of bound column", "" + column);
//                        continue;
//                    }
//                    if(row >= rows) {
//                        Log.d("out of bound row", "" + row);
//                        continue;
//                    }
//                    clusters[column][row].add(noxbox);
//                }
//                timeLogger.makeLog("split into clusters");
//
//                timeLogger = new TimeLogger();
//                // TODO (nli) create array of arrays of Cluster Noxboxes from Map of all Noxboxes
//                googleMap.clear();
//
//                for(int x = 0; x < columns; x++) {
//                    for(int y = 0; y < rows; y++) {
//                        List<Noxbox> cluster = clusters[x][y];
//                        if(cluster.isEmpty()) continue;
//                        double latitude = screen.southwest.latitude + (y + 0.5) * cellHeight;
//                        double longitude = screen.southwest.longitude + (x + 0.5) * cellWidth;
////                        if(cluster.size() == 1) {
////                            latitude = cluster.get(0).getPosition().getLatitude();
////                            longitude = cluster.get(0).getPosition().getLongitude();
////                        }
//
//
//                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude))
//                                // TODO (vl) create icon
////                                                      .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
//                                                      .anchor(0.5f, 1f);
//                        googleMap.addMarker(markerOptions).setTag(cluster);
//                    }
//                }
//                timeLogger.makeLog("map refresh");
//            }
//        });
//    }
//
//    private boolean isFiltered(Profile profile, Noxbox noxbox, LatLngBounds screen) {
////        if(noxbox.getPosition().getLongitude() < screen.southwest.longitude || noxbox.getPosition().getLongitude() > screen.northeast.longitude
////                || noxbox.getPosition().getLatitude() < screen.northeast.latitude || noxbox.getPosition().getLatitude() > screen.southwest.latitude)
////            return true;
//
//        if (profile.getDarkList().get(noxbox.getOwner().getId()) != null)
//            return true;
//        if (!profile.getFilters().getTypes().get(noxbox.getType().name()))
//            return true;
//        if (Integer.parseInt(noxbox.getPrice()) > Integer.parseInt(profile.getFilters().getPrice()))
//            return true;
//        if (noxbox.getRole() == MarketRole.demand && !profile.getFilters().getDemand())
//            return true;
//
//        if (noxbox.getRole() == MarketRole.supply && !profile.getFilters().getSupply())
//            return true;
//
//        //фильтры по типу передвижения
//        if (noxbox.getOwner().getHost() && noxbox.getOwner().getTravelMode() == TravelMode.none) {
//            if (profile.getTravelMode() == TravelMode.none)
//                return true;
//        }
//
//        if (!noxbox.getOwner().getHost()) {
//            if (!profile.getHost()) return true;
//        }
//        return false;
//    }
//
//    private double getWidth(double from, double to) {
//        double width = Math.abs(from - to);
//        if(width > 180) width -= 180;
//        return width;
//
//    }
//
//    private double getHeight(double from, double to) {
//        double height = Math.abs(from - to);
//        if(height > 90) height -= 90;
//        return height;
//
//    }


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
                    for (Noxbox noxbox : NoxboxExamples.generateNoxboxes(MapActivity.getCameraPosition(googleMap), 5000, profile)) {
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
        if (serviceIsBound) {
            activity.unbindService(mConnection);
            serviceIsBound = false;
        }
        stopListenAvailableNoxboxes();
    }

    private boolean serviceIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AvailableNoxboxesService.LocalBinder binder = (AvailableNoxboxesService.LocalBinder) service;
            AvailableNoxboxesService availableNoxboxesService = binder.getService();
            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceIsBound = false;
        }
    };

    public boolean onClusterClick(List<Noxbox> noxboxes, LatLng position) {
        DebugMessage.popup(activity, "CLUSTER");

        float newZoom = googleMap.getCameraPosition().zoom + 1f;
        if (newZoom < googleMap.getMaxZoomLevel())
            googleMap.animateCamera(newLatLngZoom(position, newZoom));
        else {
            // TODO (vl) показать список со всеми элементами кластера
        }
        return true;
    }

    public boolean onClusterItemClick(final Noxbox noxbox) {
        DebugMessage.popup(activity, "ITEM");
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                noxbox.getOwner().setPosition(noxbox.getPosition());
                profile.setViewed(noxbox);
                if (googleMap.getCameraPosition() != null) {
                    profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                }
                profile.getViewed().setParty(profile.notPublicInfo());
                Router.startActivity(activity, DetailedActivity.class);
            }
        });
        return true;
    }


    private void startListenAvailableNoxboxes() {
        GeoRealtime.startListenAvailableNoxboxes(MapActivity.getCameraPosition(googleMap).toGeoLocation(), markers);

    }


}

