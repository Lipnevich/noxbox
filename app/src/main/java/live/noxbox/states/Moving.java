package live.noxbox.states;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.ChatActivity;
import live.noxbox.activities.ConfirmationActivity;
import live.noxbox.model.Message;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.NavigatorManager;
import live.noxbox.tools.Router;
import live.noxbox.tools.location.moving.ForegroundLocationListener;

import static live.noxbox.Constants.DEFAULT_MARKER_SIZE;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.GeoRealtime.positionListener;
import static live.noxbox.database.GeoRealtime.stopListenPosition;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.DateTimeFormatter.getFormatTimeFromMillis;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;
import static live.noxbox.tools.MarkerCreator.drawMovingMemberMarker;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.location.moving.MovingWorker.cancelWorkerByTag;
import static live.noxbox.tools.location.moving.MovingWorker.runBackgroundLocationWorker;
import static live.noxbox.tools.location.moving.MovingWorker.runForegroundLocationWorker;

public class Moving implements State {

    private Activity activity;
    private Profile profile = profile();
    private TextView totalUnreadView;
    private boolean initiated;
    private boolean foregroundWorkerWasRan;

    private static GoogleMap googleMap;
    public static Position memberWhoMovingPosition;
    public static Marker memberWhoMovingMarker;
    private static LocationManager locationManager;
    private static LocationListener locationListener;

    private static LinearLayout movingView;
    private static View childMovingView;
    private static TextView timeView;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        Moving.googleMap = googleMap;
        this.activity = activity;

        if (profile.getCurrent().getConfirmationPhoto() == null) {
            if ((profile.getCurrent().getMe(profile.getId()).equals(profile.getCurrent().getOwner())
                    && (isNullOrZero(profile.getCurrent().getTimeOwnerVerified()) && isNullOrZero(profile.getCurrent().getTimeOwnerRejected())))
                    || (profile.getCurrent().getMe(profile.getId()).equals(profile.getCurrent().getParty())
                    && (isNullOrZero(profile.getCurrent().getTimePartyVerified()) && isNullOrZero(profile.getCurrent().getTimePartyRejected())))) {
                Glide.with(activity)
                        .asDrawable()
                        .load(profile.getCurrent().getNotMe(profile.getId()).getPhoto())
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                                profile.getCurrent().setConfirmationPhoto(drawable);
                            }
                        });
            }
        }

        if (memberWhoMovingPosition == null) {
            memberWhoMovingPosition = profile.getCurrent().getProfileWhoComes().getPosition();
        }

        if (!initiated) {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
            initiated = true;
        }

        if (!profile.getCurrent().getProfileWhoComes().equals(profile)) {
            provideNotification(NotificationType.moving, profile, activity.getApplicationContext());
        }

        drawUi(googleMap, activity);

        drawPath(activity, googleMap, profile.getCurrent().getPosition(), memberWhoMovingPosition);
        createCustomMarker(profile.getCurrent(), googleMap, activity.getResources(), DEFAULT_MARKER_SIZE);
        if (memberWhoMovingMarker == null) {
            memberWhoMovingMarker = drawMovingMemberMarker(profile.getCurrent().getProfileWhoComes().getTravelMode(),
                    memberWhoMovingPosition, googleMap, activity.getResources());
        } else {
            memberWhoMovingMarker.setPosition(memberWhoMovingPosition.toLatLng());
        }

        MapOperator.setNoxboxMarkerListener(googleMap, profile, activity);

        if(!foregroundWorkerWasRan){
            runForegroundLocationWorker(activity, googleMap, profile, object -> draw(googleMap, activity));
            foregroundWorkerWasRan = true;
        }
    }

    public void drawUi(GoogleMap googleMap, MapActivity activity) {
        activity.hideUi();
        activity.findViewById(R.id.container).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.chat).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.navigation).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);

        movingView = activity.findViewById(R.id.container);
        movingView.removeAllViews();
        childMovingView = activity.getLayoutInflater().inflate(R.layout.state_moving, null);
        movingView.addView(childMovingView);
        timeView = childMovingView.findViewById(R.id.timeView);
        updateTimeView(profile, activity);

        if (profile.getCurrent().getOwner().equals(profile)) {
            drawUnreadMessagesIndicator(profile.getCurrent().getChat().getPartyMessages(), profile.getCurrent().getChat().getOwnerReadTime());
        } else {
            drawUnreadMessagesIndicator(profile.getCurrent().getChat().getOwnerMessages(), profile.getCurrent().getChat().getPartyReadTime());
        }

        if (profile.getCurrent().getOwner().equals(profile)) {
            if (!isNullOrZero(profile.getCurrent().getTimeOwnerVerified())) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        } else {
            if (!isNullOrZero(profile.getCurrent().getTimePartyVerified())) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        }

        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.eye);

        activity.findViewById(R.id.navigation).setOnClickListener(v -> NavigatorManager.openNavigator(activity, profile));
        activity.findViewById(R.id.navigationImage).setOnClickListener(v -> NavigatorManager.openNavigator(activity, profile));

        activity.findViewById(R.id.locationButton).setOnClickListener(v -> {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
        });

        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> Router.startActivity(activity, ConfirmationActivity.class));

        activity.findViewById(R.id.chat).setOnClickListener(v -> startActivity(activity, ChatActivity.class));
    }


    @Override
    public void clear() {
        clearHandlers();
        clearUi();
    }

    public void clearUi() {
        clearContainer();
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.chat).setVisibility(View.GONE);
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        if (totalUnreadView != null) {
            totalUnreadView.setVisibility(View.GONE);
        }
        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.add);
    }

    @Override
    public void clearHandlers() {
        clearContainer();
        MapOperator.clearMapMarkerListener(googleMap);

        if (positionListener != null) {
            stopListenPosition(profile.getNoxboxId());
            positionListener = null;
        }
        cancelWorkerByTag(activity, ForegroundLocationListener.TAG);

        googleMap.clear();
        memberWhoMovingMarker = null;
        if (profile.getCurrent().getFinished()
                || (!isNullOrZero(profile.getCurrent().getTimePartyVerified()) && !isNullOrZero(profile.getCurrent().getTimeOwnerVerified()))) {
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
                locationListener = null;
                locationManager = null;
            }
        }
        MessagingService.removeNotifications(activity);

        runBackgroundLocationWorker(activity.getApplicationContext());
    }

    private void clearContainer() {
        if (movingView != null) {
            movingView.removeAllViews();
            movingView = null;
        }
    }

    private void drawUnreadMessagesIndicator(Map<String, Message> messages, Long readTime) {
        Integer totalUnread = 0;

        totalUnreadView = activity.findViewById(R.id.totalUnread);

        Iterator iterator = messages.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Message message = (Message) entry.getValue();

            if (message.getTime() > readTime) {
                totalUnread++;
                totalUnreadView.setVisibility(View.VISIBLE);
                if (totalUnread <= 9) {
                    totalUnreadView.setText(totalUnread.toString());
                } else {
                    totalUnreadView.setText("9+");
                }
            }
        }

        if (totalUnread == 0) {
            totalUnreadView.setVisibility(View.GONE);
        }

    }

    public static void updateTimeView(Profile profile, Context context) {
        if (movingView != null && childMovingView != null && timeView != null) {
            int progressInMinutes = ((int) getTimeInMinutesBetweenUsers(
                    profile.getCurrent().getPosition(),
                    memberWhoMovingPosition,
                    profile.getCurrent().getProfileWhoComes().getTravelMode()));
            String timeTxt = getFormatTimeFromMillis(progressInMinutes * 60000, context.getResources());
            if (profile.getCurrent().getProfileWhoComes().equals(profile)) {
                timeView.setText(context.getResources().getString(R.string.movementMove, timeTxt));
            } else {
                timeView.setText(context.getResources().getString(R.string.movementWait, timeTxt));
            }


            if (progressInMinutes <= 1
                    && !profile.getCurrent().getWasNotificationVerification()
                    && ((profile.equals(profile.getCurrent().getParty())
                    && isNullOrZero(profile.getCurrent().getTimePartyRejected())
                    && isNullOrZero(profile.getCurrent().getTimePartyVerified()))
                    || (profile.equals(profile.getCurrent().getOwner())
                    && isNullOrZero(profile.getCurrent().getTimeOwnerRejected())
                    && isNullOrZero(profile.getCurrent().getTimeOwnerVerified())))) {
                provideNotification(NotificationType.verifyPhoto, profile, context);
                profile.getCurrent().setWasNotificationVerification(true);
            }
        }
    }

    private static void provideNotification(NotificationType type, Profile profile, Context context) {
        HashMap<String, String> data = new HashMap<>();
        data.put("type", type.name());
        NotificationFactory.buildNotification(context, profile, data).show();
    }


}
