package live.noxbox.tools;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import live.noxbox.R;

public class DialogBuilder {

    public static void createSimpleAlertDialog(Activity activity, int message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(activity, R.style.NoxboxAlertDialogStyle)
                .setMessage(message)
                .setCancelable(false)
                .setTitle(R.string.note)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
