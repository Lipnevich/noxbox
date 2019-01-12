package live.noxbox.tools;

import android.app.AlertDialog;
import android.content.Context;

public class ProgressDialogManager {

    private static AlertDialog.Builder builder;
    private static AlertDialog alertDialog;

    public static void showProgress(Context context, String message) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage(message.concat("…"));
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
