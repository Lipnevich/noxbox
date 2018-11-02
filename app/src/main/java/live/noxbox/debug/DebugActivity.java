package live.noxbox.debug;

import android.util.Log;
import android.view.View;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.model.Wallet;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Task;

import static live.noxbox.state.GeoRealtime.online;
import static live.noxbox.tools.DetectNullValue.areNotTheyNull;
import static live.noxbox.tools.DetectNullValue.areTheyNull;

public class DebugActivity extends MenuActivity {
    private static final String TAG = "DebugActivity";

    @Override
    protected void onResume() {
        super.onResume();
        if (true || BuildConfig.DEBUG) {
            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    DebugActivity.this.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);

                    DebugActivity.this.findViewById(R.id.debugGenerateNoxboxes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (Noxbox noxbox : NoxboxExamples.generateNoxboxes(profile.getPosition(), 150)) {
                                online(noxbox);
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
                                        .setWallet(new Wallet().setBalance("1000"))
                                        .setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399))
                                        .setNoxboxId(profile.getCurrent().getId())
                                        .setTravelMode(TravelMode.driving)
                                        .setHost(false)
                                        .setName("Granny Smith")
                                        .setId("12321")
                                        .setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg"));
                                ProfileStorage.updateNoxbox();
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
                                profile.getCurrent().getOwner().setName("Моя бабушка курит трубку");
                                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
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
                                ProfileStorage.updateNoxbox();
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
                                ProfileStorage.updateNoxbox();
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
                                ProfileStorage.updateNoxbox();
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

}
