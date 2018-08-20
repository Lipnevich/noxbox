package live.noxbox.tools;

import android.app.ProgressDialog;

public class DialogManager {
    public static void showProgress(ProgressDialog progressDialog) {
        //TODO (vl) ProgressDialog has @Deprecated annotation, so need to find optimal solution
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Upload image to server ...");
        progressDialog.show();

    }

    public static void hideProgress(ProgressDialog progressDialog) {
        progressDialog.dismiss();
    }
}
