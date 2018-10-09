package live.noxbox.pages;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;

import live.noxbox.R;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.MessagingService;
import live.noxbox.tools.NavigatorManager;
import live.noxbox.tools.Task;

import static live.noxbox.tools.MapController.moveCopyrightLeft;
import static live.noxbox.tools.MapController.moveCopyrightRight;
import static live.noxbox.tools.Router.startActivity;

public class Moving implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private LinearLayout movingView;
    private LinearLayout photoView;
    private static CountDownTimer countDownTimer;

    public Moving(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        activity.findViewById(R.id.navigation).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigatorManager.openNavigator(activity, profile);
            }
        });


        movingView = activity.findViewById(R.id.container);
        View childMoving = activity.getLayoutInflater().inflate(R.layout.state_moving, null);
        movingView.addView(childMoving);

        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setImageResource(R.drawable.eye);
        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
            if (profile.getCurrent().getTimeOwnerVerified() != null) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        } else {
            if (profile.getCurrent().getTimePartyVerified() != null) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        }
        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setVisibility(View.VISIBLE);

        activity.findViewById(R.id.chat).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        moveCopyrightRight(googleMap);

        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugMessage.popup(activity, "way and points");
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });


        activity.findViewById(R.id.customFloatingView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.getUiSettings().setScrollGesturesEnabled(false);

                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
                activity.findViewById(R.id.chat).setVisibility(View.GONE);
                activity.findViewById(R.id.locationButton).setVisibility(View.GONE);

                photoView = movingView.findViewById(R.id.photoContainer);
                final View childPhoto = activity.getLayoutInflater().inflate(R.layout.state_moving_photo, null);
                photoView.addView(childPhoto);

                if (profile.getId().equals(profile.getCurrent().getOwner().getId())) {
                    Glide.with(activity).asDrawable().load(profile.getCurrent().getParty().getPhoto()).into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            ((ImageView) photoView.findViewById(R.id.photo)).setImageDrawable(resource);
                        }
                    });
                } else {
                    Glide.with(activity).asDrawable().load(profile.getCurrent().getOwner().getPhoto()).into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            ((ImageView) photoView.findViewById(R.id.photo)).setImageDrawable(resource);
                        }
                    });
                }
                photoView.findViewById(R.id.photoScreenClose).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        googleMap.getUiSettings().setScrollGesturesEnabled(true);

                        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
                        activity.findViewById(R.id.chat).setVisibility(View.VISIBLE);
                        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
                        photoView.removeAllViews();
                    }
                });


                final SwipeButton buttonConformity = photoView.findViewById(R.id.swipeButtonWrongPhoto);
                buttonConformity.setParametrs(activity.getDrawable(R.drawable.no), activity.getResources().getString(R.string.notLikeThat), activity);
                buttonConformity.setOnTouchListener(buttonConformity.getButtonTouchListener(new Task<Object>() {
                    @Override
                    public void execute(Object object) {
                        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                            profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                        } else {
                            profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                        }
                        googleMap.clear();
                        photoView.removeAllViews();

                        profile.getCurrent().clean();
                        ProfileStorage.fireProfile();
                    }
                }));


                final SwipeButton buttonConfirm = photoView.findViewById(R.id.swipeButtonConfirm);
                buttonConfirm.setParametrs(activity.getDrawable(R.drawable.yes), activity.getResources().getString(R.string.confirm), activity);
                buttonConfirm.setOnTouchListener(buttonConfirm.getButtonTouchListener(new Task<Object>() {
                    @Override
                    public void execute(Object object) {
                        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                            profile.getCurrent().setTimeOwnerVerified(System.currentTimeMillis());
                        } else {
                            profile.getCurrent().setTimePartyVerified(System.currentTimeMillis());
                        }

                        if (profile.getCurrent().getTimePartyVerified() != null && profile.getCurrent().getTimeOwnerVerified() != null) {
                            profile.getCurrent().setTimeStartPerforming(System.currentTimeMillis());
                        }

                        ProfileStorage.fireProfile();
                    }
                }));


            }
        });
        activity.findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(activity, ChatActivity.class);
            }
        });

        MapController.buildMapMarkerListener(googleMap, profile, activity);

        MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());

        if (profile.getCurrent().getTimeToMeet() == null) {
            final MessagingService messagingService = new MessagingService(activity.getApplicationContext());

            profile.getCurrent().setTimeToMeet((long) (Math.ceil(getTravelTimeInMinutes(profile)) * 60000));
            final Notification notification = new Notification()
                    .setTime(String.valueOf(profile.getCurrent().getTimeToMeet()))
                    .setType(NotificationType.moving)
                    .setMaxProgress((int) (long) profile.getCurrent().getTimeToMeet());
            messagingService.showPushNotification(notification);


            countDownTimer = new CountDownTimer(profile.getCurrent().getTimeToMeet(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    notification.setTime(String.valueOf(profile.getCurrent().getTimeToMeet() - (profile.getCurrent().getTimeToMeet() - millisUntilFinished)));
                    notification.setProgress((int) (profile.getCurrent().getTimeToMeet() - millisUntilFinished));
                    notification.getType().updateNotification(activity.getApplicationContext(), notification, MessagingService.builder);
                }

                @Override
                public void onFinish() {
                    final Notification notification = new Notification()
                            .setType(NotificationType.confirm);
                    messagingService.showPushNotification(notification);
                }
            }.start();
        }
    }

    private Float getDistance(Profile profile) {
        double startLat = profile.getCurrent().getParty().getPosition().getLatitude();
        double startLng = profile.getCurrent().getParty().getPosition().getLongitude();
        double endLat = profile.getCurrent().getPosition().getLatitude();
        double endLng = profile.getCurrent().getPosition().getLongitude();

        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0];
    }

    private Float getTravelTimeInMinutes(Profile profile) {
        if (profile.getCurrent().getParty().getTravelMode() != TravelMode.none && profile.getCurrent().getOwner().getTravelMode() != TravelMode.none)
            return getDistance(profile) / profile.getCurrent().getParty().getTravelMode().getSpeedInMetersPerMinute();
        if (profile.getCurrent().getOwner().getTravelMode() == TravelMode.none) {
            return getDistance(profile) / profile.getCurrent().getParty().getTravelMode().getSpeedInMetersPerMinute();
        } else {
            return getDistance(profile) / profile.getCurrent().getOwner().getTravelMode().getSpeedInMetersPerMinute();
        }
    }


    @Override
    public void clear() {
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.chat).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        moveCopyrightLeft(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        googleMap.clear();
        if (countDownTimer != null) {
            countDownTimer.cancel();

        }
        movingView.removeAllViews();
        if (photoView != null) {
            photoView.removeAllViews();
        }

    }

    @Override
    public void onDestroy() {

    }
}
