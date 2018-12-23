package live.noxbox.tools;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import live.noxbox.R;
import live.noxbox.debug.DebugMessage;
import live.noxbox.menu.profile.ProfileActivity;
import live.noxbox.model.Profile;

public class BottomSheetDialog {

    public static void openWalletAddressSheetDialog(final Activity activity, final Profile profile) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_sheet_wallet_address, null);
        final android.support.design.widget.BottomSheetDialog dialog = new android.support.design.widget.BottomSheetDialog(activity);
        dialog.setContentView(view);
        final TextView address = view.findViewById(R.id.walletAddress);
        address.setText(profile.getWallet().getAddress());
        ImageView copyToClipboard = view.findViewById(R.id.copyToClipboard);

        copyToClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugMessage.popup(activity, address.getText().toString() + " был скопирован в буфер");
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", profile.getWallet().getAddress());
                clipboard.setPrimaryClip(clip);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void openPhotoNotVerifySheetDialog(final Activity activity) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_sheet_not_verify_photo, null);
        final android.support.design.widget.BottomSheetDialog dialog = new android.support.design.widget.BottomSheetDialog(activity);
        dialog.setContentView(view);

        view.findViewById(R.id.profilePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startActivity(activity, ProfileActivity.class);
                activity.finish();
            }
        });
        dialog.show();
    }


    public static void openNameNotVerifySheetDialog(final Activity activity, final  Profile profile){
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_sheet_not_verify_name, null);
        final android.support.design.widget.BottomSheetDialog dialog = new android.support.design.widget.BottomSheetDialog(activity);
        dialog.setContentView(view);

        view.findViewById(R.id.profilePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startActivity(activity, ProfileActivity.class);
                activity.finish();
            }
        });
        dialog.show();
    }

}
