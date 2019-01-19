package live.noxbox.menu.wallet;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.functions.FirebaseFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;
import live.noxbox.model.Wallet;

import static live.noxbox.Configuration.DEFAULT_BALANCE_SCALE;
import static live.noxbox.tools.MoneyFormatter.format;
import static live.noxbox.tools.MoneyFormatter.scale;

public class WalletActivity extends BaseActivity {

    public static final int CODE = 1003;

    private static final String TAG = WalletActivity.class.getName();
    private static long lastTransferredTime;

    private EditText addressToSendEditor;
    private TextView balanceLabel;
    private TextView walletAddress;
    private ImageView copyToClipboard;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        setTitle(R.string.wallet);

        AppCache.readProfile(profile -> {
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


            balanceLabel = findViewById(R.id.balance_label_id);
            addressToSendEditor = findViewById(R.id.address_to_send_id);
            walletAddress = findViewById(R.id.wallet_address_id);
            copyToClipboard = findViewById(R.id.copy_to_clipboard_id);
            sendButton = findViewById(R.id.send_button_id);

            balanceLabel.setText(String.format(getResources().getString(R.string.balance), getString(R.string.currency)));

            walletAddress.setOnClickListener(addressToClipboardListener);
            copyToClipboard.setOnClickListener(addressToClipboardListener);
            sendButton.setOnClickListener(sendButtonOnClickListener);

            draw(profile);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.readProfile(profile -> updateBalance(profile));
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

    private void updateBalance(Profile profile) {

            if (requestQueue != null && stringRequest != null) {
                handler.postDelayed(runnable, 3000);
                return;
            }
            requestQueue = Volley.newRequestQueue(WalletActivity.this);
            String url = "https://nodes.wavesplatform.com/addresses/balance/";
            url = url.concat(profile.getWallet().getAddress());
            
            stringRequest = new StringRequest(Request.Method.GET, url, response -> {
                if (System.currentTimeMillis() - lastTransferredTime > 15000) {
                    JSONObject jObject = null;
                    String walletBalance = null;
                    try {
                        jObject = new JSONObject(response);
                        walletBalance = jObject.getString("balance");
                        BigDecimal balance = new BigDecimal(walletBalance).divide(new BigDecimal("100000000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_DOWN);
                        profile.getWallet().setBalance(balance.toString());
                        draw(profile);
                        updateBalance(profile);
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                    }
                } else {
                    updateBalance(profile);
                }
            }, Crashlytics::logException);
            stringRequest.setTag(TAG);
            requestQueue.add(stringRequest);


    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    private void transfer(Profile profile, String address) {
        if (TextUtils.isEmpty(address)) return;


        if (!address.equals(profile.getWallet().getAddressToRefund())) {
            profile.getWallet().setAddressToRefund(address);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("addressToTransfer", address);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("transfer")
                .call(data);
        profile.getWallet().setBalance("0");
        draw(profile);
        lastTransferredTime = System.currentTimeMillis();
    }


    private void draw(final Profile profile) {
        if (profile.getWallet().getAddressToRefund() != null)
            addressToSendEditor.setText(profile.getWallet().getAddressToRefund());


        final Wallet wallet = profile.getWallet();
        BigDecimal balance = scale(wallet.getBalance() != null ? new BigDecimal(wallet.getBalance()) : BigDecimal.ZERO);

        ((TextView) findViewById(R.id.balance)).setText(format(balance));

        walletAddress.setText(wallet.getAddress() != null ? wallet.getAddress() : "Not created yet");

        copyToClipboard.setVisibility(wallet.getAddress() != null ? View.VISIBLE : View.INVISIBLE);

        findViewById(R.id.inputLayout).setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);
    }

}
