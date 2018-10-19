package live.noxbox.pages;

import android.view.View;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.model.Wallet;
import live.noxbox.state.Firestore;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Task;

import static live.noxbox.tools.DetectNullValue.areNotTheyNull;
import static live.noxbox.tools.DetectNullValue.areTheyNull;

public class DebugActivity extends MenuActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    DebugActivity.this.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);

                    findViewById(R.id.debugRequest).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (areNotTheyNull(profile.getCurrent(), profile.getCurrent().getOwner(), profile.getCurrent().getTimeCreated())
                                    && profile.getCurrent().getTimeRequested() == null) {
                                // TODO (vl) сгенерировать коменты, сертификаты, примеры работ
                                profile.getCurrent().setParty(new Profile().setWallet(new Wallet().setBalance("1000")).setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399)).setNoxboxId(profile.getNoxboxId()).setTravelMode(TravelMode.driving).setHost(false).setName("Granny Smith").setId("12321").setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg"));
                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                Firestore.writeNoxbox(profile.getCurrent());
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to request");
                            }

                        }
                    });

                    findViewById(R.id.debugAccept).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null && !profile.getCurrent().getOwner().getId().equals(profile.getId())
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() == null) {
                                profile.getCurrent().getOwner().setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg");
                                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to accept");
                            }
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
                                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                                    profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                                }
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to reject");
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

                                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                                    profile.getCurrent().setTimePartyVerified(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
                                }
                                if (profile.getCurrent().getTimeOwnerVerified() != null &&
                                        profile.getCurrent().getTimePartyVerified() != null) {
                                    profile.getCurrent().setTimeStartPerforming(System.currentTimeMillis());
                                }
                                Firestore.writeNoxbox(profile.getCurrent());
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to verify");
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
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to complete");
                            }
                        }
                    });
                }
            });
        }


    }

}
