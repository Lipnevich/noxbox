package live.noxbox.pages;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import java.text.DecimalFormat;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.Task;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private int seconds;
    private double currency;
    private Handler handler;
    private Runnable runnable;
    private DecimalFormat decimalFormat = new DecimalFormat("###.###");

    public Performing(Activity activity,GoogleMap googleMap) {
        this.activity = activity;
        this.googleMap = googleMap;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        activity.findViewById(R.id.pathButton).setVisibility(View.VISIBLE);
        MarkerCreator.createCustomMarker(profile.getCurrent(),googleMap,activity,profile.getTravelMode());
        activity.findViewById(R.id.pathButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(),15));
            }
        });

        activity.findViewById(R.id.performingScreen).setVisibility(View.VISIBLE);
        seconds = (int) ((System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        currency = Double.parseDouble(profile.getCurrent().getPrice()) / 4;
        drawPrice(profile);
        drawComplete(profile);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);

                ((TextView) activity.findViewById(R.id.timeView)).setText(time);

                seconds++;
                drawPrice(profile);
                handler.postDelayed(this, 1000);
            }
        };

        runTimer();
    }

    private void drawPrice(Profile profile) {
        if (profile.getCurrent().getTimeStartPerforming() >=
                System.currentTimeMillis() - Configuration.MINIMUM_PAYMENT_TIME_MILLIS) {
            ((TextView) activity.findViewById(R.id.currency)).setText(decimalFormat.format(currency));
        } else {
            currency = currency + ((Double.parseDouble(profile.getCurrent().getPrice()) / profile.getCurrent().getType().getMin()) / 60);
            ((TextView) activity.findViewById(R.id.currency)).setText(decimalFormat.format(currency));
        }
    }

    private void drawComplete(final Profile profile) {
        SwipeButton completeSwipeButton = activity.findViewById(R.id.completeSwipeButton);
        completeSwipeButton.setText(activity.getResources().getString(R.string.completeText));
        completeSwipeButton.setOnTouchListener(completeSwipeButton.getButtonTouchListener(new Task<Object>() {
            @Override
            public void execute(Object object) {
                stopTimer();
                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());

                Long totalTimeInMillis = profile.getCurrent().getTimeCompleted() - profile.getCurrent().getTimeStartPerforming();
                Long timeInMinutes = (totalTimeInMillis / (1000 * 60)) % 60;

                profile.getCurrent().setTotalExecutionTimeInMinutes(timeInMinutes);
                profile.getCurrent().setPrice(decimalFormat.format(currency));

                DebugMessage.popup(activity, String.valueOf(timeInMinutes) + "minutes");
                profile.setCurrent(ProfileStorage.noxbox());
                ProfileStorage.fireProfile();
            }
        }));
    }

    private void runTimer() {
        handler.post(runnable);
    }

    private void stopTimer() {
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.pathButton).setVisibility(View.GONE);
        activity.findViewById(R.id.performingScreen).setVisibility(View.GONE);
    }
}
