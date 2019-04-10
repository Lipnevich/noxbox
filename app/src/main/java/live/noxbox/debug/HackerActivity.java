package live.noxbox.debug;

import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.database.Firestore;
import live.noxbox.menu.MenuActivity;
import live.noxbox.model.NotificationType;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.model.Wallet;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.debug.DebugMessage.popup;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.ReferrerCatcher.createLink;

public class HackerActivity extends MenuActivity {

    private List<NotificationType> photoPushes = Arrays.asList(NotificationType.values());
    private Iterator<NotificationType> iterator = photoPushes.iterator();
    private Profile party = new Profile()
            .setId("12321")
            .setWallet(new Wallet().setBalance("1000000"))
            .setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399))
            .setTravelMode(TravelMode.bicycling)
            .setHost(true)
            .setName("Granny Smith")
            .setPhoto(NoxboxExamples.PHOTO_MOCK);


    @Override
    protected void onResume() {
        super.onResume();
        if (!BuildConfig.DEBUG) return;

        AppCache.listenProfile(HackerActivity.class.getName(), i -> {
            HackerActivity.this.findViewById(R.id.debugNotify).setVisibility(View.GONE);
            HackerActivity.this.findViewById(R.id.debugGenerateNoxboxes).setVisibility(View.GONE);
            HackerActivity.this.findViewById(R.id.debugRequest).setVisibility(View.GONE);
            HackerActivity.this.findViewById(R.id.debugAccept).setVisibility(View.GONE);
            HackerActivity.this.findViewById(R.id.debugPhotoVerify).setVisibility(View.GONE);
            HackerActivity.this.findViewById(R.id.debugPhotoReject).setVisibility(View.GONE);
            HackerActivity.this.findViewById(R.id.debugComplete).setVisibility(View.GONE);

            NoxboxState currentState = NoxboxState.getState(profile().getCurrent(), profile());
            switch (currentState) {
                case initial:
                    HackerActivity.this.findViewById(R.id.debugNotify).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugNotify, o -> {
                        createLink(profile().getId());
                    });

                    findViewById(R.id.debugGenerateNoxboxes).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugGenerateNoxboxes, o -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().build());

                        db.collection("profiles").document(profile().getId()).set(Firestore.objectToMap(profile()), SetOptions.merge())
                                .addOnCompleteListener(task -> {
                                    if (task.getException() != null) {
                                        popup(getApplicationContext(), task.getException().getMessage());
                                    } else {
                                        popup(getApplicationContext(), "GOOD JOB");
                                    }
                                });
                    });
                    break;
                case created:
                    HackerActivity.this.findViewById(R.id.debugRequest).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugRequest, o -> {
                        profile().getCurrent().setTimeRequested(System.currentTimeMillis());
                        profile().getCurrent().setParty(party.setNoxboxId(profile().getCurrent().getId()));
                        AppCache.updateNoxbox();
                    });
                    break;
                case requesting:
                    HackerActivity.this.findViewById(R.id.debugAccept).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugAccept, o -> {
                        profile().getCurrent().getOwner().setPhoto(NoxboxExamples.PHOTO_MOCK);
                        profile().getCurrent().getOwner().setId("" + ThreadLocalRandom.current().nextInt(100000));
                        profile().getCurrent().getOwner().setName("Моя бабушка курит трубку");
                        profile().getCurrent().setTimeAccepted(System.currentTimeMillis());
                        AppCache.updateNoxbox();
                    });
                    break;
                case accepting:
                    break;
                case moving:
                    if (isNullOrZero(profile().getCurrent().getTimeOwnerVerified()) &&
                            isNullOrZero(profile().getCurrent().getTimePartyVerified()))
                        break;

                    HackerActivity.this.findViewById(R.id.debugPhotoReject).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugPhotoReject, o -> {
                        if (profile().equals(profile().getCurrent().getOwner())) {
                            profile().getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                        } else {
                            profile().getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                            profile().getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
                        }
                        AppCache.updateNoxbox();
                    });

                    HackerActivity.this.findViewById(R.id.debugPhotoVerify).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugPhotoVerify, o -> {
                        if (profile().equals(profile().getCurrent().getOwner())) {
                            profile().getCurrent().setTimePartyVerified(System.currentTimeMillis());
                        } else {
                            profile().getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
                        }
                        AppCache.updateNoxbox();
                    });
                    break;
                case performing:
                    HackerActivity.this.findViewById(R.id.debugComplete).setVisibility(View.VISIBLE);
                    setOnClickListener(R.id.debugComplete, o -> {
                        profile().getCurrent().setTimeCompleted(System.currentTimeMillis());
                        AppCache.updateNoxbox();
                    });
            }
        });
    }

    private void setOnClickListener(int button, final Task<Void> task) {
        findViewById(button).setOnLongClickListener(v -> {
            task.execute(null);
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(HackerActivity.class.getName());
    }
}
