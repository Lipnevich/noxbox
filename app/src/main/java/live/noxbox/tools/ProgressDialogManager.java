package live.noxbox.tools;

import android.app.Activity;
import android.app.AlertDialog;

import live.noxbox.R;

public class ProgressDialogManager {

    private static AlertDialog.Builder builder;
    private static AlertDialog alertDialog;

    public static void showProgress(Activity activity) {
        if(activity.isFinishing()) return;

        builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.photoValidationProgressContent));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public static void hideProgress() {
        if (builder != null && alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
            builder = null;
        }

    }
}
