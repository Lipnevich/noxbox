package live.noxbox.performing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.Task;

public class PerformingActivity extends AppCompatActivity {

    private static final String SECONDS = "seconds";
    private static final String RUNNING = "running";
    private int seconds;
    private boolean running;
    private static Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.performing));
        setContentView(R.layout.activity_state_performing);

        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(RUNNING);
            seconds = savedInstanceState.getInt(SECONDS);
        }

        ProfileStorage.listenProfile(PerformingActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
                runTimer();
            }
        });

    }

    private void draw(final Profile profile) {
        drawStart(profile);
        drawPrice(profile);
        drawComplete(profile);
    }

    private void drawStart(Profile profile) {
        running = true;
    }

    private void drawPrice(Profile profile) {
        ((TextView) findViewById(R.id.currency)).setText(profile.getCurrent().getPrice());
    }

    private void drawComplete(final Profile profile) {
        findViewById(R.id.complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotify();
                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());

                Long totalTimeInMillis = profile.getCurrent().getTimeCompleted() - profile.getCurrent().getTimeStartPerforming();
                Long timeInMinutes = (totalTimeInMillis / (1000 * 60)) % 60;

                profile.getCurrent().setTotalExecutionTimeInMinutes(timeInMinutes);

                running = false;

                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);

                mBuilder = new Builder(PerformingActivity.this)
                        .setSmallIcon(R.drawable.noxbox)
                        .setContentTitle(getString(R.string.performing))
                        .setContentText(time);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(PerformingActivity.this);

                stackBuilder.addParentStack(PerformingActivity.class);

                stackBuilder.addNextIntent(new Intent(PerformingActivity.this, PerformingActivity.class));
                mBuilder.setContentIntent(stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                ));
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(2, mBuilder.build());

                ((TextView) findViewById(R.id.timeView)).setText(time);

                if (running) {
                    seconds++;
                }

                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        cancelNotify();
        bundle.putInt(SECONDS, seconds);
        bundle.putBoolean(RUNNING, running);
    }

    private void cancelNotify() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
}
