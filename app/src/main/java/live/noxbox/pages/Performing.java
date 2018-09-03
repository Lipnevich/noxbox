package live.noxbox.pages;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;

public class Performing implements State {

    private Activity activity;
    private int seconds;
    private double currency;
    private Handler handler;
    private Runnable runnable;
    private DecimalFormat decimalFormat = new DecimalFormat("###.##");

    public Performing(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.performingScreen).setVisibility(View.VISIBLE);
        seconds = (int) ((System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        currency = Integer.parseInt(profile.getCurrent().getPrice()) / 4;
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
        if (profile.getCurrent().getTimeStartPerforming() >= System.currentTimeMillis() - Configuration.MINIMUM_PAYMENT_TIME_MILLIS) {
            ((TextView) activity.findViewById(R.id.currency)).setText(decimalFormat.format(currency));
        } else {
            currency = currency + ((Double.parseDouble(profile.getCurrent().getPrice()) / profile.getCurrent().getType().getMin()) / 60);
            ((TextView) activity.findViewById(R.id.currency)).setText(decimalFormat.format(currency));
        }
    }

    private void drawComplete(final Profile profile) {
        activity.findViewById(R.id.complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());

                Long totalTimeInMillis = profile.getCurrent().getTimeCompleted() - profile.getCurrent().getTimeStartPerforming();
                Long timeInMinutes = (totalTimeInMillis / (1000 * 60)) % 60;

                profile.getCurrent().setTotalExecutionTimeInMinutes(timeInMinutes);
                profile.getCurrent().setPrice(decimalFormat.format(currency));

                DebugMessage.popup(activity, String.valueOf(timeInMinutes) + "minutes");
                ProfileStorage.fireProfile();
            }
        });
    }

    private void runTimer() { handler.post(runnable); }

    private void stopTimer() { handler.removeCallbacksAndMessages(null); }


    @Override
    public void clear() { activity.findViewById(R.id.performingScreen).setVisibility(View.GONE); }
}
