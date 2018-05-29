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

import java.util.SortedMap;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.RequestType;
import by.nicolay.lipnevich.noxbox.pages.WalletPerformerPage;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.IntentAndKey;
import by.nicolay.lipnevich.noxbox.tools.QRCaptureActivity;
import by.nicolay.lipnevich.noxbox.tools.Timer;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.createHistory;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.like;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.sendRequest;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNotAcceptedNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateProfile;
import static by.nicolay.lipnevich.noxbox.tools.PageCodes.WALLET;
import static java.lang.System.currentTimeMillis;

public abstract class PerformerActivity extends PerformerLocationActivity {

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
                new IntentIntegrator(PerformerActivity.this)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                        .setOrientationLocked(false)
                        .setCaptureActivity(QRCaptureActivity.class)
                        .initiateScan();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                popup("Failed to recognize qr code, please try again");
            } else {
                // TODO (nli) check is it correct it on server, process responce, update current Noxbox
                tryGetNoxboxInProgress().getPayers().values().iterator().next().setSecret(result.getContents());
                scanQr.setVisibility(View.INVISIBLE);
                updateCurrentNoxbox(tryGetNoxboxInProgress());
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

    protected void prepareForIteration() {
        super.prepareForIteration();

        Noxbox notAcceptednoxbox = tryGetNotAcceptedNoxbox();
        if(notAcceptednoxbox != null) {
            processRequest(notAcceptednoxbox);
            return;
        }

        if(tryGetNoxboxInProgress() != null) {
            removeCurrentNoxbox();
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
                        .setType(RequestType.complete)
                        .setPayer(tryGetNoxboxInProgress().getPayers().values().iterator().next().publicInfo())
                        .setPerformer(getProfile().publicInfo())
                        .setNoxbox(new Noxbox().setPrice(tryGetNoxboxInProgress().getPrice())
                                .setId(noxbox.getId())));
                like();
                createHistory(tryGetNoxboxInProgress().setTimeCompleted(currentTimeMillis()));
                prepareForIteration();
            }
        });

        drawPathToNoxbox(getProfile().setPosition(getCurrentPosition()), noxbox);

        completeButton.setVisibility(View.VISIBLE);
        completeButton.setEnabled(isQrRecognized());
        completeButton.setBackgroundResource(isQrRecognized() ? R.color.primary : R.color.divider);

        scanQr.setVisibility(View.VISIBLE);

        acceptButton.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
    }

    protected void processPing(final Message pingMessage) {
        pingMessage.getNoxbox().setEstimationTime(pingMessage.getEstimationTime());
        processRequest(pingMessage.getNoxbox());
    }

    protected void processRequest(final Noxbox noxbox) {
        if(tryGetNoxboxInProgress() != null) {
            cancelPing(tryGetNoxboxInProgress());
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
                if(tryGetNotAcceptedNoxbox() != null) {
                    cancelPing(noxbox);
                }
            }
        }.start(remainSecondToAccept);

        for(Profile payer : noxbox.getPayers().values()) {
            Position performerPosition = getCurrentPosition() != null ? getCurrentPosition() :
                    noxbox.getPerformers().get(getProfile().getId()).getPosition();

            drawPath(null, getProfile().setPosition(performerPosition),
                    new Profile().setId(payer.getId()).setPosition(noxbox.getPosition()));
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptButton.setVisibility(View.INVISIBLE);
                getProfile().getNoxboxesForPerformer().put(noxboxType().toString(),
                        noxbox.setTimeAccepted(System.currentTimeMillis()));
                updateProfile(new Profile().setNoxboxesForPerformer(getProfile().getNoxboxesForPerformer())
                        .setId(getProfile().getId()));
                timer.stop();
                goOffline();
                cleanUpMap();

                String estimation = tryGetNoxboxInProgress().getEstimationTime();

                Firebase.sendRequest(new Request()
                        .setType(RequestType.accept)
                        .setPayer(tryGetNoxboxInProgress().getPayers().values().iterator().next().publicInfo())
                        .setPerformer(getProfile().publicInfo())
                        .setEstimationTime(estimation)
                        .setNoxbox(new Noxbox().setId(tryGetNoxboxInProgress().getId())));
                processNoxbox(tryGetNoxboxInProgress());
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

    private void cancelPing(Noxbox noxbox) {
        sendRequest(new Request().setType(RequestType.cancel)
                .setPerformer(getProfile().publicInfo())
                .setPayer(noxbox.getPayers().values().iterator().next().publicInfo())
                .setNoxbox(new Noxbox().setId(noxbox.getId()))
                .setReason("Canceled by performer"));
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
    protected void goOnline() {
        super.goOnline();

        goOfflineButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void processGnop(Message gnop) {
        prepareForIteration();
    }

    @Override
    protected SortedMap<String, IntentAndKey> getMenu() {
        SortedMap<String, IntentAndKey> menu = super.getMenu();

        menu.put(getString(R.string.wallet), new IntentAndKey()
                .setIntent(new Intent(getApplicationContext(), WalletPerformerPage.class))
                .setKey(WALLET.getCode()));

        return menu;
    }

}
