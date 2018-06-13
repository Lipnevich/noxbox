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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import by.nicolay.lipnevich.noxbox.model.Event;
import by.nicolay.lipnevich.noxbox.model.EventType;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.pages.QRCapturePage;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Timer;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.persistHistory;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.sendRequest;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateCurrentNoxbox;

public abstract class PerformerFunction extends PerformerLocationFunction {

    private Button completeButton;
    private Button goOnlineButton;
    private Button goOfflineButton;
    private Button acceptButton;
    private ImageView pathImage;
    private ImageView scanQr;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goOnlineButton = findViewById(R.id.goOnlineButton);
        goOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goOnline();
            }
        });

        goOfflineButton = findViewById(R.id.goOfflineButton);
        goOfflineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goOffline();
            }
        });

        acceptButton = findViewById(R.id.acceptButton);
        completeButton = findViewById(R.id.completeButton);

        pathImage = findViewById(R.id.pathImage);
        scanQr = findViewById(R.id.scanQrCode);
        scanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new IntentIntegrator(PerformerFunction.this)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                        .setOrientationLocked(false)
                        .setCaptureActivity(QRCapturePage.class)
                        .initiateScan();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                DebugMessage.popup(this,"Failed to recognize qr code, please try again");
            } else {
                // TODO (nli) check is it correct it on server, process responce, update current Noxbox
                getCurrentNoxbox().getPayer().setSecret(result.getContents());
                scanQr.setVisibility(View.INVISIBLE);
                updateCurrentNoxbox(getCurrentNoxbox());
                completeButton.setEnabled(isQrRecognized());
                completeButton.setBackgroundResource(isQrRecognized() ? R.color.primary : R.color.divider);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private boolean isQrRecognized() {
        // TODO (nli) check qr via request
        return true;
    }

    public void prepareForIteration() {
        super.prepareForIteration();

        Noxbox currentNoxbox = getCurrentNoxbox();
        if(currentNoxbox != null) {
            if(currentNoxbox.getTimeAccepted() == null) {
                processRequest(currentNoxbox);
            } else {
                removeCurrentNoxbox();
            }
        }
        goOffline();
    }

    @Override
    public void processNoxbox(final Noxbox noxbox) {
        super.processNoxbox(noxbox);
        // TODO swipe button for Android?
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeButton.setVisibility(View.INVISIBLE);
                sendRequest(new Request()
                        .setType(EventType.complete)
                        .setNoxbox(noxbox));
                persistHistory();
                prepareForIteration();
            }
        });

        noxbox.getPerformer().setPosition(getCurrentPosition());
        drawPathToNoxbox(noxbox);

        completeButton.setVisibility(View.VISIBLE);
        completeButton.setEnabled(isQrRecognized());
        completeButton.setBackgroundResource(isQrRecognized() ? R.color.primary : R.color.divider);

        scanQr.setVisibility(View.VISIBLE);

        acceptButton.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
    }

    protected void processRequest(final Event request) {
        request.getNoxbox().setEstimationTime(request.getEstimationTime());
        processRequest(request.getNoxbox());
    }

    protected void processRequest(final Noxbox noxbox) {
        if(getCurrentNoxbox() != null) {
            cancelRequest(getCurrentNoxbox());
            return;
        }

        int remainSecondToAccept = remainSecondToAccept(noxbox);
        if(remainSecondToAccept < 1) {
            removeCurrentNoxbox();
            prepareForIteration();
            return;
        }

        updateCurrentNoxbox(noxbox);
        if(timer != null) {
            timer.stop();
        }
        timer = new Timer() {
            @Override
            protected void timeout() {
                if(getCurrentNoxbox().getTimeAccepted() == null) {
                    cancelRequest(noxbox);
                }
            }
        }.start(remainSecondToAccept);

        drawPathToNoxbox(noxbox);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptButton.setVisibility(View.INVISIBLE);
                updateCurrentNoxbox(noxbox.setTimeAccepted(System.currentTimeMillis()));
                timer.stop();
                goOffline();
                cleanUpMap();

                Firebase.sendRequest(new Request()
                        .setType(EventType.accept)
                        .setNoxbox(noxbox));
                processNoxbox(noxbox);
            }
        });
        acceptButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
    }

    private int remainSecondToAccept(Noxbox noxbox) {
        if(noxbox == null) return 0;
        int allowedTimeForAccept = getResources().getInteger(R.integer.timer_in_seconds);
        int secondsAfterRequestPassed = (int) ((System.currentTimeMillis() - noxbox.getTimeRequested()) / 1000);
        return allowedTimeForAccept - secondsAfterRequestPassed;
    }

    private void cancelRequest(Noxbox noxbox) {
        sendRequest(new Request().setType(EventType.performerCancel)
                .setNoxbox(noxbox));
        prepareForIteration();
    }

    @Override
    protected void goOffline() {
        super.goOffline();

        goOnlineButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void goOnline() {
        super.goOnline();

        goOfflineButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void processPerformerCancel(Event performerCancel) {
        prepareForIteration();
    }

}
