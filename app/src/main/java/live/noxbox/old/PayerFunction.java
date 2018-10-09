package live.noxbox.old;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.math.BigDecimal;

import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.Request;

import static live.noxbox.state.Firebase.removeCurrentNoxbox;
import static live.noxbox.state.Firebase.sendRequest;

public class PayerFunction extends FragmentActivity {

    private Button requestButton;
    private Button cancelButton;

    private ImageView pointerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pointerImage = findViewById(R.id.pointerImage);
        requestButton = findViewById(R.id.requestButton);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final Noxbox bestOption = chooseBestOptionPerformer();
//                if(bestOption != null) {
//                    if(!isEnoughMoney(bestOption.getPrice())) {
//                        startActivityForResult(new Intent(getApplicationContext(), WalletActivity.class), WalletActivity.CODE);
//                        return;
//                    }
//
//                    requestButton.setVisibility(View.INVISIBLE);
//                    pointerImage.setVisibility(View.INVISIBLE);
//                    drawPath(null, bestOption.getPerformer(), getProfile().publicInfo().setPosition(getCameraPosition()));
//
//                    sendRequest(new Request().setType(NotificationType.request).setNoxbox(bestOption));
//
//                    timer = new Timer() {
//                        @Override
//                        protected void timeout() {
//                            processTimeout();
//                            prepareForIteration();
//                        }
//                    }.start(getResources().getInteger(R.integer.timer_in_seconds) * 120 / 100);
//                } else {
//                    popup("No performers available");
//                }
            }
        });

        // cancelButton = findViewById(R.id.cancelButton);
    }

    private void processTimeout(Noxbox currentNoxbox) {
        removeCurrentNoxbox();
    }

    private boolean isEnoughMoney(Profile profile, Noxbox noxbox) {
        return new BigDecimal(profile.getWallet().getBalance())
                .compareTo(new BigDecimal(noxbox.getPrice())) >= 0;
    }

    public void prepareForIteration(Profile profile) {
        processTimeout(profile.getCurrent());

        if (profile.getCurrent() != null) {
            removeCurrentNoxbox();
        }

        requestButton.setVisibility(View.VISIBLE);
        pointerImage.setVisibility(View.VISIBLE);

        cancelButton.setVisibility(View.INVISIBLE);
    }

    public void processNoxbox(final Noxbox noxbox) {
//        stopListenAvailablePerformers();
//        drawPathToNoxbox(noxbox);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PayerFunction.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.cancelPrompt));
                builder.setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendRequest(new Request().setType(NotificationType.demanderCanceled)
                                        .setNoxbox(noxbox));
                            }
                        });
                builder.setNegativeButton(R.string.no, null);
                builder.show();
            }
        });
        cancelButton.setVisibility(View.VISIBLE);

        requestButton.setVisibility(View.INVISIBLE);
        pointerImage.setVisibility(View.INVISIBLE);
    }

}
