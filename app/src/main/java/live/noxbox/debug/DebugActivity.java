package live.noxbox.debug;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.BuildConfig;
import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.model.Wallet;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Task;

import static live.noxbox.database.GeoRealtime.online;
import static live.noxbox.tools.DetectNullValue.areNotTheyNull;
import static live.noxbox.tools.DetectNullValue.areTheyNull;

public class DebugActivity extends MenuActivity implements
        OnMapReadyCallback {
    private static final String TAG = "DebugActivity";

    private List<NotificationType> photoPushes = Arrays.asList(NotificationType.photoUploadingProgress,
            NotificationType.photoValidationProgress,
            NotificationType.photoValid, NotificationType.photoInvalid);
    private Iterator<NotificationType> iterator = photoPushes.iterator();


    @Override
    protected void onResume() {
        super.onResume();
        if (true || BuildConfig.DEBUG) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    profile.getWallet().setBalance("10000000");

                    DebugActivity.this.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);


                    DebugActivity.this.findViewById(R.id.debugPush).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Map<String, String> data = new HashMap<>();
                            if(!iterator.hasNext()) iterator = photoPushes.iterator();
                            data.put("type", iterator.next().name());
                            data.put("progress", "" + 50);
                            NotificationFactory.buildNotification(DebugActivity.this, profile, data).show();
                        }
                    });

                    DebugActivity.this.findViewById(R.id.debugGenerateNoxboxes).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    double delta = (360 * Configuration.RADIUS_IN_METERS / 40075000) / 20;

                                    for (Noxbox noxbox : NoxboxExamples.generateNoxboxes(Position.from(googleMap.getCameraPosition().target), 150, delta)) {
                                        online(noxbox);
                                    }

                                    LatLng myPosition = null;
                                    if (ContextCompat.checkSelfPermission(DebugActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED) {
                                        //myPosition = profile.getPosition().toLatLng();
                                        myPosition = googleMap.getCameraPosition().target;
                                    }

                                    if (myPosition != null) {

                                        LatLng coordinatesOne = new LatLng(myPosition.latitude + delta, myPosition.longitude + delta);
                                        LatLng coordinatesTwo = new LatLng(myPosition.latitude + delta, myPosition.longitude - delta);
                                        LatLng coordinatesThree = new LatLng(myPosition.latitude - delta, myPosition.longitude - delta);
                                        LatLng coordinatesFour = new LatLng(myPosition.latitude - delta, myPosition.longitude + delta);
                                        googleMap.addPolyline(new PolylineOptions().geodesic(true).add(
                                                coordinatesOne,
                                                coordinatesTwo,
                                                coordinatesThree,
                                                coordinatesFour,
                                                coordinatesOne)
                                                .color(Color.RED)
                                                .width(5));
                                    }
                                }
                            });

                    findViewById(R.id.debugRequest).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (areNotTheyNull(profile.getCurrent(), profile.getCurrent().getOwner(), profile.getCurrent().getTimeCreated())
                                    && profile.getCurrent().getTimeRequested() == null) {
                                // TODO (vl) сгенерировать коменты, сертификаты, примеры работ

                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                profile.getCurrent().setParty(new Profile()
                                        .setWallet(new Wallet().setBalance("1000000"))
                                        .setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399))
                                        .setNoxboxId(profile.getCurrent().getId())
                                        .setTravelMode(TravelMode.driving)
                                        .setHost(false)
                                        .setName("Granny Smith")
                                        .setId("12321")
                                        .setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg"));
                                AppCache.updateNoxbox();
                                GeoRealtime.offline(profile.getCurrent());
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to request");
                            }


                            Log.d(State.TAG + TAG, "debugRequest");
                            Log.d(State.TAG + TAG, "noxboxId: " + profile.getCurrent().getId());
                            Log.d(State.TAG + TAG, "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
                            Log.d(State.TAG + TAG, "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));


                        }
                    });

                    findViewById(R.id.debugAccept).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null && !profile.equals(profile.getCurrent().getOwner())
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() == null) {
                                profile.getCurrent().getOwner().setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg");
                                profile.getCurrent().getOwner().setId("" + ThreadLocalRandom.current().nextInt(100000));
                                profile.getCurrent().getOwner().setName("Моя бабушка курит трубку");
                                profile.getCurrent().setTimeToMeet(1200000L);
                                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                                AppCache.updateNoxbox();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to accept");
                            }
                            Log.d(State.TAG + TAG, "debugAccept");
                            Log.d(State.TAG + TAG, "noxboxId: " + profile.getCurrent().getId());
                            Log.d(State.TAG + TAG, "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
                            Log.d(State.TAG + TAG, "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
                            Log.d(State.TAG + TAG, "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));
                        }
                    });

                    findViewById(R.id.debugPhotoReject).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() != null
                                    && profile.getCurrent().getTimeCompleted() == null) {
                                if (profile.equals(profile.getCurrent().getOwner())) {
                                    profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                                }
                                AppCache.updateNoxbox();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to reject");
                            }

                            Log.d(State.TAG + TAG, "debugPhotoReject");
                            Log.d(State.TAG + TAG, "noxboxId: " + profile.getCurrent().getId());
                            Log.d(State.TAG + TAG, "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
                            Log.d(State.TAG + TAG, "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
                            Log.d(State.TAG + TAG, "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));
                            if (profile.equals(profile.getCurrent().getOwner())) {
                                Log.d(State.TAG + TAG, "timeCanceled: " + DateTimeFormatter.time(profile.getCurrent().getTimeCanceledByParty()));
                            } else {
                                Log.d(State.TAG + TAG, "timeCanceled: " + DateTimeFormatter.time(profile.getCurrent().getTimeCanceledByOwner()));
                            }

                        }
                    });

                    findViewById(R.id.debugPhotoVerify).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (areNotTheyNull(
                                    profile.getCurrent(),
                                    profile.getCurrent().getTimeCreated(),
                                    profile.getCurrent().getTimeRequested(),
                                    profile.getCurrent().getTimeAccepted())
                                    && areTheyNull(
                                    profile.getCurrent().getTimeStartPerforming(),
                                    profile.getCurrent().getTimeCompleted())) {

                                if (profile.equals(profile.getCurrent().getOwner())) {
                                    profile.getCurrent().setTimePartyVerified(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
                                }
                                AppCache.updateNoxbox();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to verify");
                            }
                            Log.d(State.TAG + TAG, "debugPhotoVerify");
                            Log.d(State.TAG + TAG, "noxboxId: " + profile.getCurrent().getId());
                            Log.d(State.TAG + TAG, "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
                            Log.d(State.TAG + TAG, "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
                            Log.d(State.TAG + TAG, "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));
                            if (profile.equals(profile.getCurrent().getOwner())) {
                                Log.d(State.TAG + TAG, "timePartyVerified: " + DateTimeFormatter.time(profile.getCurrent().getTimePartyVerified()));
                            } else {
                                Log.d(State.TAG + TAG, "timeOwnerVerified: " + DateTimeFormatter.time(profile.getCurrent().getTimeOwnerVerified()));
                            }

                        }
                    });

                    findViewById(R.id.debugComplete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() != null
                                    && profile.getCurrent().getTimeCompleted() == null) {
                                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());
                                AppCache.updateNoxbox();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to complete");
                            }
                            Log.d(State.TAG + TAG, "debugComplete");
                            Log.d(State.TAG + TAG, "noxboxId: " + profile.getCurrent().getId());
                            Log.d(State.TAG + TAG, "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
                            Log.d(State.TAG + TAG, "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
                            Log.d(State.TAG + TAG, "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));
                            if (profile.equals(profile.getCurrent().getOwner())) {
                                Log.d(State.TAG + TAG, "timePartyVerified: " + DateTimeFormatter.time(profile.getCurrent().getTimePartyVerified()));
                            } else {
                                Log.d(State.TAG + TAG, "timeOwnerVerified: " + DateTimeFormatter.time(profile.getCurrent().getTimeOwnerVerified()));
                            }
                            Log.d(State.TAG + TAG, "timeCompleted: " + DateTimeFormatter.time(profile.getCurrent().getTimeCompleted()));
                        }
                    });

                }
            });
        }


    }

    private GoogleMap googleMap;

    @Override
    public void onMapReady(GoogleMap readyMap) {
        googleMap = readyMap;

    }
}
