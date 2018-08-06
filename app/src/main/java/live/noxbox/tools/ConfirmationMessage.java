package live.noxbox.tools;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import live.noxbox.R;

public class ConfirmationMessage {
    private static Snackbar online;
    private static Snackbar gps;

    public static void messageOffline(final Activity activity) {
        if (online == null) {
            online = Snackbar.make(activity.findViewById(android.R.id.content), R.string.isOffline, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.enable, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            activity.startActivity(intent);
                        }
                    });
        }
        showMessage(online, activity);
    }

    public static void messageGps(final Activity activity) {
        if (gps == null) {
            gps = Snackbar.make(activity.findViewById(android.R.id.content), R.string.isGps, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.enable, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivity(intent);
                        }
                    });
        }
        showMessage(gps, activity);
    }

    private static void showMessage(Snackbar snackbar, final Activity activity) {
        View view = snackbar.getView();
        TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextSize(14);
        textView.setTextColor(activity.getResources().getColor(R.color.secondary));
        TextView action = view.findViewById(android.support.design.R.id.snackbar_action);
        action.setTextSize(14);
        action.setTypeface(null, Typeface.BOLD);
        action.setTextColor(activity.getResources().getColor(R.color.primary));
        snackbar.show();
    }

    public static void dismissOfflineMessage() {
        if (online != null) {
            online.dismiss();
        }
    }

    public static void dismissGpsMessage() {
        if (gps != null) {
            gps.dismiss();
        }
    }
}
