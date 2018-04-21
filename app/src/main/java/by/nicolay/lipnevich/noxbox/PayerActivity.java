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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.math.BigDecimal;
import java.util.SortedMap;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.RequestType;
import by.nicolay.lipnevich.noxbox.pages.WalletPage;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.IntentAndKey;
import by.nicolay.lipnevich.noxbox.tools.Timer;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getWallet;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.removeMessage;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateCurrentNoxbox;
import static by.nicolay.lipnevich.noxbox.tools.PageCodes.WALLET;

public abstract class PayerActivity extends NoxboxActivity {

    private Button requestButton;
    private Button cancelButton;

    private ImageView pointerImage;
    private ImageView showQrCode;
    private Timer timer;
    private EditText messageText;
    private View.OnClickListener qrListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            String secret = tryGetNoxboxInProgress().getPayers()
                    .get(getProfile().getId()).getSecret();

            try {
                BitMatrix bitMatrix = new MultiFormatWriter().encode(secret,
                        BarcodeFormat.QR_CODE,512,512);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                showQrCode.setImageBitmap(bitmap);

                final int initialHeight = showQrCode.getHeight();
                final int initialWidth = showQrCode.getWidth();

                showQrCode.getLayoutParams().height = 512;
                showQrCode.getLayoutParams().width = 512;
                showQrCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showQrCode.getLayoutParams().height = initialHeight;
                        showQrCode.getLayoutParams().width = initialWidth;
                        showQrCode.setImageDrawable(getResources().getDrawable(R.drawable.qr));
                        showQrCode.setOnClickListener(qrListener);
                    }
                });
            } catch (WriterException ignore) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pointerImage = findViewById(R.id.pointerImage);
        pointerImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), getPayerDrawable()));

        requestButton = findViewById(R.id.requestButton);
        showQrCode = findViewById(R.id.showQrCode);
        showQrCode.setOnClickListener(qrListener);

        updatePrice();

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEnoughMoney()) {
                    startActivityForResult(new Intent(getApplicationContext(), WalletPage.class), WALLET.getCode());
                    return;
                }
                final Profile bestOption = chooseBestOptionPerformer();
                if(bestOption != null) {
                    requestButton.setVisibility(View.INVISIBLE);
                    pointerImage.setVisibility(View.INVISIBLE);
                    googleMap.setPadding(0,0,0,0);
                    drawPath(null, bestOption, getProfile().publicInfo().setPosition(getCameraPosition()));

                    Firebase.sendRequest(new Request()
                        .setType(RequestType.request)
                        .setPosition(getCameraPosition())
                        .setPerformer(new Profile().setId(bestOption.getId()).setPosition(bestOption.getPosition())));

                    timer = new Timer() {
                        @Override
                        protected void timeout() {
                            // TODO (nli) retry request to next best option
                            prepareForIteration();
                        }
                    }.start(getResources().getInteger(R.integer.timer_in_seconds) + 4);
                } else {
                    popup("No performers available");
                }
            }
        });

        cancelButton = findViewById(R.id.cancelButton);
        messageText = findViewById(R.id.message);
    }

    public void updatePrice() {
        if(requestButton == null) return;

        if(Firebase.getPrice() == null) {
            requestButton.setText(getResources().getText(noxboxType().getMessage()));
        } else {
            requestButton.setText(getResources().getText(noxboxType().getMessage())
                    + " " + Firebase.getPrice() + " " + getResources().getString(R.string.crypto_currency));
        }
    }

    private boolean isEnoughMoney() {
        return new BigDecimal(getWallet().getBalance())
                .compareTo(Firebase.getPrice()) >= 0;
    }

    public void prepareForIteration() {
        super.prepareForIteration();

        listenAvailablePerformers();

        if(tryGetNoxboxInProgress() != null) {
            removeCurrentNoxbox();
        }

        requestButton.setVisibility(View.VISIBLE);
        updatePrice();
        pointerImage.setVisibility(View.VISIBLE);

        cancelButton.setVisibility(View.INVISIBLE);
        messageText.setVisibility(View.INVISIBLE);
        showQrCode.setVisibility(View.INVISIBLE);
    }

    @Override
    public void processNoxbox(final Noxbox noxbox) {
        super.processNoxbox(noxbox);
        stopListenAvailablePerformers();
        drawPathsToAllPerformers(noxbox);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PayerActivity.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.cancelPrompt));
                builder.setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Firebase.sendRequest(new Request().setType(RequestType.cancel)
                                        .setNoxbox(new Noxbox().setId(noxbox.getId()))
                                        .setReason("Canceled by payer " + getProfile().getName()));
                                prepareForIteration();
                            }
                        });
                builder.setNegativeButton(R.string.no, null);
                builder.show();
                }
        });
        cancelButton.setVisibility(View.VISIBLE);
        showQrCode.setVisibility(View.VISIBLE);

        messageText.setVisibility(View.INVISIBLE);
        messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Firebase.addPayerMessage(getProfile().getId(), v.getText().toString());
                    v.setText("");

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

        requestButton.setVisibility(View.INVISIBLE);
        pointerImage.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void processPong(Message pong) {
        if(timer != null) {
            timer.stop();
        }
        cleanUpMap();
        updateCurrentNoxbox(pong.getNoxbox());
        removeMessage(pong.getId());
        processNoxbox(pong.getNoxbox());
    }

    protected void processGnop(Message gnop) {
        // TODO (nli) retry instead
        removeMessage(gnop.getId());
        prepareForIteration();    }

    @Override
    protected void processMove(Message move) {
        Noxbox noxbox = tryGetNoxboxInProgress();
        if(noxbox != null) {
            noxbox.getPerformers().get(move.getSender().getId())
                    .setPosition(move.getSender().getPosition());
            updateCurrentNoxbox(noxbox);
        }
        removeMessage(move.getId());
    }

    @Override
    protected SortedMap<String, IntentAndKey> getMenu() {
        SortedMap<String, IntentAndKey> menu = super.getMenu();

        menu.put(getString(R.string.wallet), new IntentAndKey()
                .setIntent(new Intent(getApplicationContext(), WalletPage.class))
                .setKey(WALLET.getCode()));

        return menu;
    }
}
