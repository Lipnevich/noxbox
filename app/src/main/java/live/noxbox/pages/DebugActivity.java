package live.noxbox.pages;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Task;

public class DebugActivity extends MenuActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(final Profile profile) {
                    DebugActivity.this.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);

//                    findViewById(R.id.debugGenerateNoxboxes).setVisibility(View.VISIBLE);
//                    findViewById(R.id.debugGenerateNoxboxes).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            for (Noxbox noxbox : NoxboxExamples.generateNoxboxes(new Position().setLongitude(27.569018).setLatitude(53.871399), 150, profile)) {
//                                // TODO (nli) создать AvailableServicesStorage, добавить туда слушателя, добавить ноксбоксы и зажечь
////                                createMarker(profile, noxbox);
//                            }
//                        }
//                    });

                    findViewById(R.id.debugRequest).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null && profile.getCurrent().getOwner().getId().equals(profile.getId())
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() == null) {

                                profile.getCurrent().setParty(new Profile().setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399)).setTravelMode(TravelMode.driving).setHost(false).setName("Granny Smith").setId("12321").setPhoto("http://fit4brain.com/wp-content/uploads/2014/06/zelda.jpg"));
                                // TODO (vl) сгенерировать коменты, сертификаты, примеры работ
                                profile.getCurrent().setTimeRequested(System.currentTimeMillis());
                                ProfileStorage.fireProfile();
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
                                ProfileStorage.fireProfile();
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
                                ProfileStorage.fireProfile();

                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to reject");
                            }
                        }
                    });

                    findViewById(R.id.debugPhotoVerify).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (profile.getCurrent() != null
                                    && profile.getCurrent().getTimeCreated() != null
                                    && profile.getCurrent().getTimeRequested() != null
                                    && profile.getCurrent().getTimeAccepted() != null
                                    && profile.getCurrent().getTimeCompleted() == null) {
                                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                                    profile.getCurrent().setTimePartyVerified(System.currentTimeMillis());
                                } else {
                                    profile.getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
                                }
                                if (profile.getCurrent().getTimeOwnerVerified() != null &&
                                        profile.getCurrent().getTimePartyVerified() != null) {
                                    profile.getCurrent().setTimeStartPerforming(System.currentTimeMillis());
                                }

                                ProfileStorage.fireProfile();
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
                                profile.getCurrent().clean();
                                ProfileStorage.fireProfile();
                            } else {
                                DebugMessage.popup(DebugActivity.this, "Not possible to complete");
                            }
                        }
                    });
                    findViewById(R.id.debugYandexNavi).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("yandexnavi://build_route_on_map?"
                                    + "lat_from="
                                    + profile.getPosition().getLatitude()
                                    + "&lon_from="
                                    + profile.getPosition().getLongitude()
                                    + "&lat_to="
                                    + profile.getCurrent().getPosition().getLatitude()
                                    + "&lon_to="
                                    + profile.getCurrent().getPosition().getLongitude()))
                                    .setPackage("ru.yandex.yandexnavi");

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=ru.yandex.yandexnavi")));
                            }
                        }
                    });
                    findViewById(R.id.debugGoogleNavi).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:"
                                    + "q="
                                    + profile.getCurrent().getPosition().getLatitude()
                                    + ", "
                                    + profile.getCurrent().getPosition().getLongitude()
                                    + "&mode=b"))
                                    .setPackage("com.google.android.apps.maps");
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.google.android.apps.maps")));
                            }

                        }
                    });
                }
            });
        }


    }

}
