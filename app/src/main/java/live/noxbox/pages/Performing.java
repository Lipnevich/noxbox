package live.noxbox.pages;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.text.DecimalFormat;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MessagingService;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.START_TIME;
import static live.noxbox.tools.MapController.buildMapPosition;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private int seconds;
    private double totalMoney;
    private Handler handler;
    private Runnable runnable;
    private DecimalFormat decimalFormat = new DecimalFormat("###.###");
    private LinearLayout performingView;

    public Performing(Activity activity, GoogleMap googleMap) {
        this.activity = activity;
        this.googleMap = googleMap;
    }

    @Override
    public void draw(final Profile profile) {
        buildMapPosition(googleMap, profile, activity.getApplicationContext());

        performingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_performing, null);
        performingView.addView(child);

        seconds = (int) ((System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        totalMoney = Double.parseDouble(profile.getCurrent().getPrice()) / 4;
        drawPrice(profile);
        drawComplete(profile);

        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification()
                .setType(NotificationType.performing)
                .setTime(START_TIME)
                .setPrice(drawPrice(profile));
        messagingService.showPushNotification(notification);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                if (profile.getCurrent().getTimeCompleted() == null) {
                    ((TextView) performingView.findViewById(R.id.timeView)).setText(time);
                    notification.setTime(time);
                    notification.getType().updateNotification(activity.getApplicationContext(), notification, MessagingService.builder, messagingService);
                    seconds++;
                    drawPrice(profile);
                    handler.postDelayed(this, 1000);
                }
            }
        };

        runTimer();
    }


    private String drawPrice(Profile profile) {
        final Long startTime = profile.getCurrent().getTimeStartPerforming();

        if (startTime == null) return null;

        double pricePerSecond = Double.parseDouble(profile.getCurrent().getPrice()) / profile.getCurrent().getType().getDuration() / 60;

        if (startTime >= System.currentTimeMillis() - Configuration.MINIMUM_PAYMENT_TIME_MILLIS) {
            ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(decimalFormat.format(totalMoney));
        } else {
            totalMoney = ((System.currentTimeMillis() - startTime) / 1000) * pricePerSecond;
            ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(decimalFormat.format(totalMoney));
        }
        return ((TextView) performingView.findViewById(R.id.moneyToPay)).getText().toString();

    }

    private void drawComplete(final Profile profile) {
        SwipeButton completeSwipeButton = performingView.findViewById(R.id.completeSwipeButton);
        completeSwipeButton.setText(activity.getResources().getString(R.string.completeText));
        completeSwipeButton.setParametrs(activity.getDrawable(R.drawable.yes), activity.getResources().getString(R.string.completeText), activity);
        completeSwipeButton.setOnTouchListener(completeSwipeButton.getButtonTouchListener(new Task<Object>() {
            @Override
            public void execute(Object object) {
                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());

                Long totalTimeInMillis = profile.getCurrent().getTimeCompleted() - profile.getCurrent().getTimeStartPerforming();
                profile.getCurrent().setPrice(decimalFormat.format(totalMoney));

                Long timeInMinutes = (totalTimeInMillis / (1000 * 60)) % 60;
                DebugMessage.popup(activity, String.valueOf(timeInMinutes) + "minutes");

                profile.getCurrent().clean(); //while debug without estimating
                ProfileStorage.fireProfile();
            }
        }));
    }

    private void runTimer() {
        handler.post(runnable);
    }

    private void stopTimer() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }


    @Override
    public void clear() {
        googleMap.clear();
        stopTimer();
        performingView.removeAllViews();
    }
}
