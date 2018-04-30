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
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import by.nicolay.lipnevich.noxbox.performer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.IntentAndKey;
import by.nicolay.lipnevich.noxbox.tools.QRCaptureActivity;
import by.nicolay.lipnevich.noxbox.tools.Timer;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.createHistory;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.like;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeMessage;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.sendRequest;
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
    private EditText messageText;
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
        messageText = findViewById(R.id.message);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                popup("Failed to recognize qr code, please try again");
            } else {
                // TODO (nli) check is it correct it on server, process responce, update current Noxbox
                tryGetNoxboxInProgress().getPayers().values().iterator().next().setSecret(result.getContents());
                updateCurrentNoxbox(tryGetNoxboxInProgress());
                completeButton.setEnabled(isQrRecognized());
                completeButton.setBackgroundResource(isQrRecognized() ? R.color.primary : R.color.divider);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private boolean isQrRecognized() {
        Profile payer = tryGetNoxboxInProgress().getPayers().values().iterator().next();
        return payer != null && payer.getSecret() != null;
    }

    protected void prepareForIteration() {
        super.prepareForIteration();

        if(tryGetNoxboxInProgress() != null) {
            removeCurrentNoxbox();
        }
        if(getIntent().getExtras() == null || !getIntent().getExtras().getBoolean(ONLINE)) {
            goOffline();
        }
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
                        .setNoxbox(new Noxbox().setId(noxbox.getId())));
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
        messageText.setVisibility(View.INVISIBLE);
        messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Firebase.addPerformerMessage(getProfile().getId(), v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        acceptButton.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
    }

    protected Noxbox tryGetNoxboxInProgress() {
        if(getProfile() != null && getProfile().getNoxboxesForPerformer() != null) {
            return getProfile().getNoxboxesForPerformer().get(noxboxType().toString());
        }
        return null;
    }

    protected void processPing(final Message pingMessage) {

        if(tryGetNoxboxInProgress() != null || System.currentTimeMillis() - pingMessage.getTime() > getResources().getInteger(R.integer.timer_in_seconds) * 1000) {
            cancelPing(pingMessage);
            return;
        }

        if(timer != null) {
            timer.stop();
        }
        timer = new Timer() {
            @Override
            protected void timeout() {
                if(tryGetNoxboxInProgress() == null || tryGetNoxboxInProgress().getTimeAccepted() == null) {
                    cancelPing(pingMessage);
                }
            }
        }.start(getResources().getInteger(R.integer.timer_in_seconds));

        for(Profile payer : pingMessage.getNoxbox().getPayers().values()) {
            Position performerPosition = getCurrentPosition() != null ? getCurrentPosition() :
                    pingMessage.getNoxbox().getPerformers().get(getProfile().getId()).getPosition();

            drawPath(null, getProfile().setPosition(performerPosition),
                    new Profile().setId(payer.getId()).setPosition(pingMessage.getNoxbox().getPosition()));
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptButton.setVisibility(View.INVISIBLE);
                getProfile().getNoxboxesForPerformer().put(noxboxType().toString(),
                        pingMessage.getNoxbox().setTimeAccepted(System.currentTimeMillis()));
                updateProfile(new Profile().setNoxboxesForPerformer(getProfile().getNoxboxesForPerformer())
                        .setId(getProfile().getId()));
                timer.stop();
                goOffline();
                cleanUpMap();

                Firebase.sendRequest(new Request()
                        .setType(RequestType.accept)
                        .setNoxbox(new Noxbox().setId(pingMessage.getNoxbox().getId())));
                removeMessage(pingMessage.getId());
                processNoxbox(pingMessage.getNoxbox());
            }
        });
        acceptButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
    }

    private void cancelPing(Message message) {
        sendRequest(new Request().setType(RequestType.cancel).setNoxbox(new Noxbox().setId(message.getNoxbox().getId())));
        removeMessage(message.getId());

        Bundle state = new Bundle();
        state.putBoolean(ONLINE, false);
        getIntent().putExtras(state);

        prepareForIteration();
    }

    private static final String ONLINE = "Online";

    @Override
    protected void goOffline() {
        super.goOffline();
        Bundle state = new Bundle();
        state.putBoolean(ONLINE, false);
        getIntent().putExtras(state);

        goOnlineButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOfflineButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
        messageText.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void goOnline() {
        super.goOnline();
        Bundle state = new Bundle();
        state.putBoolean(ONLINE, true);
        getIntent().putExtras(state);

        goOfflineButton.setVisibility(View.VISIBLE);

        pathImage.setVisibility(View.INVISIBLE);
        scanQr.setVisibility(View.INVISIBLE);
        goOnlineButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        completeButton.setVisibility(View.INVISIBLE);
        messageText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        if(getIntent().getExtras() != null && getIntent().getExtras().getBoolean(ONLINE)) {
            goOnline();
        }
    }

    @Override
    protected void processGnop(Message gnop) {
        removeMessage(gnop.getId());

        Bundle state = new Bundle();
        state.putBoolean(ONLINE, false);
        getIntent().putExtras(state);

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
