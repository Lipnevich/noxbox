/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package by.nicolay.lipnevich.noxbox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;

import java.math.BigDecimal;

import by.nicolay.lipnevich.noxbox.model.Event;
import by.nicolay.lipnevich.noxbox.model.EventType;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.tools.Timer;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getWallet;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.sendRequest;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateCurrentNoxbox;

public class PayerFunction extends FragmentActivity {

    private Button requestButton;
    private Button cancelButton;

    private ImageView pointerImage;
    private ImageView showQrCode;
    private Timer timer;

    // TODO move qr logic to separate activity
    private int initialQrHeight;
    private int initialQrWidth;

    private View.OnClickListener qrListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(getCurrentNoxbox() == null) return;

            String secret = getCurrentNoxbox().getPayer().getSecret();

            final int size = 512;
            showQrCode.setImageBitmap(QRCode.from(secret).withSize(size, size).bitmap());
            initialQrHeight = showQrCode.getLayoutParams().height;
            initialQrWidth = showQrCode.getLayoutParams().width;

            showQrCode.getLayoutParams().height = 512;
            showQrCode.getLayoutParams().width = 512;
            showQrCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showQrCode.getLayoutParams().height = initialQrHeight;
                    showQrCode.getLayoutParams().width = initialQrWidth;

                    showQrCode.setImageDrawable(getResources().getDrawable(R.drawable.qr));
                    showQrCode.setOnClickListener(qrListener);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pointerImage = findViewById(R.id.pointerImage);
        requestButton = findViewById(R.id.requestButton);
        showQrCode = findViewById(R.id.showQrCode);
        showQrCode.setOnClickListener(qrListener);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final Noxbox bestOption = chooseBestOptionPerformer();
//                if(bestOption != null) {
//                    if(!isEnoughMoney(bestOption.getPrice())) {
//                        startActivityForResult(new Intent(getApplicationContext(), WalletPage.class), WalletPage.CODE);
//                        return;
//                    }
//
//                    requestButton.setVisibility(View.INVISIBLE);
//                    pointerImage.setVisibility(View.INVISIBLE);
//                    drawPath(null, bestOption.getPerformer(), getProfile().publicInfo().setPosition(getCameraPosition()));
//
//                    sendRequest(new Request().setType(EventType.request).setNoxbox(bestOption));
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

        cancelButton = findViewById(R.id.cancelButton);
    }

    private void processTimeout() {
        Noxbox currentNoxbox = getCurrentNoxbox();
        if(currentNoxbox != null && currentNoxbox.getTimeAccepted() == null) {
            sendRequest(new Request()
                    .setType(EventType.payerCancel)
                    .setNoxbox(currentNoxbox)
                    .setMessage("Canceled by timeout " + getProfile().getName()));
        }
        removeCurrentNoxbox();
    }

    private boolean isEnoughMoney(String price) {
        return new BigDecimal(getWallet().getBalance())
                .compareTo(new BigDecimal(price)) >= 0;
    }

    public void prepareForIteration() {
        processTimeout();

        if(getCurrentNoxbox() != null) {
            removeCurrentNoxbox();
        }

        requestButton.setVisibility(View.VISIBLE);
        pointerImage.setVisibility(View.VISIBLE);

        cancelButton.setVisibility(View.INVISIBLE);
        showQrCode.setImageDrawable(getResources().getDrawable(R.drawable.qr));
        if(initialQrHeight != 0) {
            showQrCode.getLayoutParams().height = initialQrHeight;
            showQrCode.getLayoutParams().width = initialQrWidth;
        }
        showQrCode.setVisibility(View.INVISIBLE);
    }

    protected void processSync(Event sync) {
        updateCurrentNoxbox(sync.getNoxbox());
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
                                sendRequest(new Request().setType(EventType.payerCancel)
                                        .setNoxbox(noxbox));
                                prepareForIteration();
                            }
                        });
                builder.setNegativeButton(R.string.no, null);
                builder.show();
                }
        });
        cancelButton.setVisibility(View.VISIBLE);
        showQrCode.setVisibility(View.VISIBLE);

        requestButton.setVisibility(View.INVISIBLE);
        pointerImage.setVisibility(View.INVISIBLE);
    }

    protected void processAccept(Event accept) {
        if(timer != null) {
            timer.stop();
        }
//        cleanUpMap();
        updateCurrentNoxbox(accept.getNoxbox());
        processNoxbox(accept.getNoxbox());
    }

    protected void processPerformerCancel(Event cancel) {
        // TODO (nli) retry instead
        prepareForIteration();
    }

    protected void processMove(Event move) {
        Noxbox noxbox = getCurrentNoxbox();
        if(noxbox != null) {
            noxbox.getMe(move.getSender().getId()).setPosition(move.getSender().getPosition());
            updateCurrentNoxbox(noxbox);
//            drawPathToNoxbox(noxbox);
        }
    }

}
