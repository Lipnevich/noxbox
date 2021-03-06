package live.noxbox.menu.wallet;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;
import com.google.firebase.functions.FirebaseFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Profile;
import live.noxbox.model.Wallet;
import live.noxbox.tools.DisplayMetricsConservations;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static live.noxbox.Constants.DEFAULT_BALANCE_SCALE;
import static live.noxbox.analitics.BusinessEvent.outBox;
import static live.noxbox.database.AppCache.fireProfile;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.AppCache.showPriceInUsd;
import static live.noxbox.tools.MoneyFormatter.format;
import static live.noxbox.tools.MoneyFormatter.scale;

public class WalletActivity extends BaseActivity {
    static boolean active = false;

    public static final int CODE = 1003;

    private static final String TAG = WalletActivity.class.getName();

    private ImageButton homeButton;
    private TextView title;
    private EditText addressToSendEditor;
    private TextView balanceLabel;
    private TextView currency;
    private TextView balance;
    private TextView balanceUSD;
    private ImageView progressCat;
    private TextView walletAddress;
    private ImageView copyToClipboard;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        setTitle(R.string.wallet);

        homeButton = findViewById(R.id.homeButton);
        title = findViewById(R.id.title);
        balanceLabel = findViewById(R.id.balanceText);
        currency = findViewById(R.id.currency);
        addressToSendEditor = findViewById(R.id.address_to_send_id);
        walletAddress = findViewById(R.id.walletAddress);
        copyToClipboard = findViewById(R.id.copy_to_clipboard_id);
        sendButton = findViewById(R.id.send_button_id);
        balance = findViewById(R.id.balance);
        balanceUSD = findViewById(R.id.balanceUSD);
        progressCat = findViewById(R.id.progress);

        int progressDiameter = DisplayMetricsConservations.pxToDp(balance.getHeight(), getApplicationContext());
        Glide.with(this)
                .asGif()
                .load(R.drawable.progress_cat)
                .apply(RequestOptions.overrideOf(progressDiameter, progressDiameter))
                .into(progressCat);

        if (profile().getWallet().getAddressToRefund() != null)
            addressToSendEditor.setText(profile().getWallet().getAddressToRefund());

    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(WalletActivity.class.getName(), profile -> {
            View.OnClickListener addressToClipboardListener = view -> {
                if (profile.getWallet().getAddress() == null) return;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null) return;

                clipboard.setPrimaryClip(ClipData.newPlainText("noxboxWalletAddress", profile.getWallet().getAddress()));
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.addressInClipBoard), Toast.LENGTH_LONG).show();
            };

            View.OnClickListener sendButtonOnClickListener = view -> {
                if (TextUtils.isEmpty(addressToSendEditor.getText())) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.sendPrompt));
                builder.setPositiveButton(getResources().getString(R.string.transfer),
                        (dialog, which) -> transfer(profile, addressToSendEditor.getText().toString()));
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            };

            balanceLabel.setText(getResources().getString(R.string.balanceText));
            String currency = getString(R.string.currency);
            String link = getString(R.string.wavesProductsWalletLink);

            showSpannableText(currency, object -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link))), this.currency);

            showSpannableText(getResources().getString(R.string.walletAddress), 4, object -> openWavesExplorerForWallet(profile.getWallet().getAddress()), findViewById(R.id.walletAddressTitle));

            walletAddress.setOnClickListener(addressToClipboardListener);
            copyToClipboard.setOnClickListener(addressToClipboardListener);
            sendButton.setOnClickListener(sendButtonOnClickListener);

            draw(profile);
            if (!Strings.isNullOrEmpty(profile.getWallet().getAddress())) {
                updateBalance(profile);
            }
        });
    }

    private void showSpannableText(String text, Task<Object> click, TextView spanView) {
        showSpannableText(text, 0, click, spanView);
    }

    private void showSpannableText(String text, final int startPosition, Task<Object> click, TextView spanView) {
        SpannableStringBuilder currencySpan = new SpannableStringBuilder(text);
        currencySpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary)), startPosition, currency.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        currencySpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                click.execute(null);
            }
        }, startPosition, currencySpan.length(), 0);

        spanView.setMovementMethod(LinkMovementMethod.getInstance());
        spanView.setText(currencySpan, TextView.BufferType.SPANNABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        AppCache.stopListen(WalletActivity.class.getName());
    }

    private StringRequest stringRequest;
    private RequestQueue requestQueue;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestQueue.add(stringRequest);
        }
    };
    private static Boolean transferInProgress;
    private static long delayMillis = 4000;

    private void updateBalance(Profile profile) {
        if (requestQueue != null && stringRequest != null) {
            handler.postDelayed(runnable, delayMillis);
            return;
        }
        enableTransfer(false);

        requestQueue = Volley.newRequestQueue(this);
        String url = "https://nodes.wavesplatform.com/addresses/balance/";
        url = url.concat(profile.getWallet().getAddress());

        stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            JSONObject jObject = null;
            String walletBalance = null;
            try {
                jObject = new JSONObject(response);
                walletBalance = jObject.getString("balance");
                BigDecimal balance = new BigDecimal(walletBalance).divide(new BigDecimal("100000000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_DOWN);
                profile.getWallet().setBalance(balance.toString());
                delayMillis = 30000;
                fireProfile();
                hideProgress();

            } catch (JSONException e) {
                Crashlytics.logException(e);
            }
        }, error -> {
            Crashlytics.logException(error);
        });
        stringRequest.setTag(TAG);
        requestQueue.add(stringRequest);
    }


    private void transfer(Profile profile, String address) {
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.pleaseEnterWalletAddress), Toast.LENGTH_LONG).show();
            return;
        }

        if (!address.equals(profile.getWallet().getAddressToRefund())) {
            profile.getWallet().setAddressToRefund(address);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("addressToTransfer", address);

        showProgress();
        enableTransfer(false);

        transferInProgress = true;
        FirebaseFunctions.getInstance()
                .getHttpsCallable("transfer")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {
                    BusinessActivity.businessEvent(outBox);
                    requestQueue.cancelAll(TAG);

                    profile.getWallet().setBalance("0");
                    fireProfile();
                    balance.setText(profile.getWallet().getBalance());

                    enableTransfer(false);
                    transferInProgress = false;
                    hideProgress();
                })
                .addOnFailureListener(e -> {
                    transferInProgress = false;
                    hideProgress();
                    enableTransfer(true);
                });
    }

    private void showProgress() {
        if (!isFinishing() && progressCat != null && balance != null && balanceUSD != null) {
            balance.setVisibility(View.INVISIBLE);
            balanceUSD.setVisibility(View.INVISIBLE);
            progressCat.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (!isFinishing() && progressCat != null && balance != null && balanceUSD != null
                && (transferInProgress == null || !transferInProgress)) {
            progressCat.setVisibility(View.INVISIBLE);
            balance.setVisibility(View.VISIBLE);
            balanceUSD.setVisibility(View.VISIBLE);
        }
    }

    private void draw(final Profile profile) {
        drawToolbar();

        final Wallet wallet = profile.getWallet();
        BigDecimal balance = scale(wallet.getBalance() != null ? new BigDecimal(wallet.getBalance()) : BigDecimal.ZERO);

        this.balance.setText(format(balance));
        this.balanceUSD.setText(showPriceInUsd("", balance.toString()));

        walletAddress.setText(wallet.getAddress() != null ? wallet.getAddress() : "Not created yet");

        copyToClipboard.setVisibility(wallet.getAddress() != null ? View.VISIBLE : View.INVISIBLE);

        enableTransfer(balance.compareTo(BigDecimal.ZERO) > 0
                && (profile.getCurrent().getMyRole(profile.getId()) == MarketRole.supply
                || profile.getCurrent().getFinished()));
    }

    private void drawToolbar() {
        homeButton.setOnClickListener(v -> Router.finishActivity(WalletActivity.this));
        title.setText(R.string.wallet);
    }

    private void enableTransfer(boolean enable) {
        findViewById(R.id.send_button_id).setEnabled(enable);
        findViewById(R.id.address_to_send_id).setEnabled(enable);
    }

    private void openWavesExplorerForWallet(String walletAddress) {
        String url = "https://wavesexplorer.com/address/" + walletAddress;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
