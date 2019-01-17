package live.noxbox.menu.wallet;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import live.noxbox.tools.Task;

import static live.noxbox.tools.MoneyFormatter.format;
import static live.noxbox.tools.MoneyFormatter.scale;

public class WalletActivity extends BaseActivity {

    public static final int CODE = 1003;

    private EditText addressToSendEditor;

    private static final String TAG = WalletActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        setTitle(R.string.wallet);
        addressToSendEditor = findViewById(R.id.address_to_send_id);
//        if(getProfile() != null && getProfile().getAddressToRefund() != null) {
//            addressToSendEditor.setText(getProfile().getAddressToRefund());
//        }
        // TODO (nli) send request to blockchain directly instead
//        GeoRealtime.sendRequest(new Request().setType(NotificationType.balance));
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                recalculateBalance(profile);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                updateBalance(profile);
            }
        });
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
    private long lastTransferTime;

    private void updateBalance(Profile profile) {

        if (requestQueue != null && stringRequest != null) {
            handler.postDelayed(runnable, 3000);
            return;
        }
        requestQueue = Volley.newRequestQueue(WalletActivity.this);
        String url = "https://nodes.wavesplatform.com/addresses/balance/";
        url = url.concat(profile.getWallet().getAddress());

        stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            if (System.currentTimeMillis() - lastTransferTime > 10000) {
                JSONObject jObject = null;
                String walletBalance = null;
                try {
                    jObject = new JSONObject(response);
                    walletBalance = jObject.getString("balance");
                    BigDecimal balance = new BigDecimal(walletBalance).divide(new BigDecimal("100000000"));
                    profile.getWallet().setBalance(balance.toString());
                    recalculateBalance(profile);
                    updateBalance(profile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                updateBalance(profile);
            }
        }, error -> Crashlytics.logException(error));
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
                .call(data)
                .continueWith(task -> {
                    String result = (String) task.getResult().getData();
                    return result;
                });
        profile.getWallet().setBalance("0");
        recalculateBalance(profile);
        lastTransferTime = System.currentTimeMillis();
    }

    private void recalculateBalance(final Profile profile) {
        if (profile.getWallet().getAddressToRefund() != null) {
            addressToSendEditor.setText(profile.getWallet().getAddressToRefund());
        }
        // TODO (nli) send request to blockchain directly instead

        final Wallet wallet = profile.getWallet();
        BigDecimal balance = wallet.getBalance() != null ? new BigDecimal(wallet.getBalance()) : BigDecimal.ZERO;
        BigDecimal frozenMoney = wallet.getFrozenMoney() != null ? new BigDecimal(wallet.getFrozenMoney()) : BigDecimal.ZERO;
        balance = scale(balance.subtract(frozenMoney));

        TextView balanceLabel = findViewById(R.id.balance_label_id);
        balanceLabel.setText(String.format(getResources().getString(R.string.balance), getString(R.string.currency)));

        TextView balanceText = findViewById(R.id.balance_id);
        balanceText.setText(format(balance));

        View.OnClickListener addressToClipboardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wallet.getAddress() == null) return;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null) return;

                clipboard.setPrimaryClip(ClipData.newPlainText("walletAddress", wallet.getAddress()));
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.addressInClipBoard), Toast.LENGTH_SHORT).show();
            }
        };

        TextView walletAddress = findViewById(R.id.wallet_address_id);
        walletAddress.setText(wallet.getAddress() != null ? wallet.getAddress() : "Not created yet");
        walletAddress.setOnClickListener(addressToClipboardListener);

        ImageView copyToClipboard = findViewById(R.id.copy_to_clipboard_id);
        copyToClipboard.setVisibility(wallet.getAddress() != null ? View.VISIBLE : View.INVISIBLE);
        copyToClipboard.setOnClickListener(addressToClipboardListener);

        Button sendButton = findViewById(R.id.send_button_id);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(addressToSendEditor.getText())) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.sendPrompt));
                builder.setPositiveButton(getResources().getString(R.string.transfer),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                transfer(profile, addressToSendEditor.getText().toString());
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
        });
        sendButton.setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);

        EditText addressToSendEditor = findViewById(R.id.address_to_send_id);
        addressToSendEditor.setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);

    }

}
