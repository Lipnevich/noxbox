package by.nicolay.lipnevich.noxbox.menu;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.EventType;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.Wallet;
import by.nicolay.lipnevich.noxbox.state.Firebase;
import by.nicolay.lipnevich.noxbox.state.ProfileStorage;
import by.nicolay.lipnevich.noxbox.tools.Task;

import java.math.BigDecimal;

import static by.nicolay.lipnevich.noxbox.Configuration.CURRENCY;
import static by.nicolay.lipnevich.noxbox.tools.MoneyFormatter.format;
import static by.nicolay.lipnevich.noxbox.tools.MoneyFormatter.scale;

public class WalletActivity extends AppCompatActivity {

    public static final int CODE = 1003;

    private EditText addressToSendEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallet);
        setTitle(R.string.wallet);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ProfileStorage.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                recalculateBalance(profile);
            }
        });

        addressToSendEditor = findViewById(R.id.address_to_send_id);
//        if(getProfile() != null && getProfile().getAddressToRefund() != null) {
//            addressToSendEditor.setText(getProfile().getAddressToRefund());
//        }
        // TODO (nli) send request to blockchain directly instead
        Firebase.sendRequest(new Request().setType(EventType.balance));
    }

    private void refund(Profile profile, String address) {
        if(TextUtils.isEmpty(address)) return;

        if(!address.equals(profile.getWallet().getAddressToRefund())) {
            profile.getWallet().setAddressToRefund(address);
        }

        Firebase.sendRequest(new Request()
                .setType(EventType.refund)
                .setAddress(profile.getWallet().getAddressToRefund()));
    }

    private void recalculateBalance(final Profile profile) {
        final Wallet wallet = profile.getWallet();
        BigDecimal balance = wallet.getBalance() != null ? new BigDecimal(wallet.getBalance()) : BigDecimal.ZERO;
        BigDecimal frozenMoney = wallet.getFrozenMoney() != null ? new BigDecimal(wallet.getFrozenMoney()) : BigDecimal.ZERO;
        balance = scale(balance.subtract(frozenMoney));

        TextView balanceLabel = findViewById(R.id.balance_label_id);
        balanceLabel.setText(String.format(getResources().getString(R.string.balance), CURRENCY));

        TextView balanceText = findViewById(R.id.balance_id);
        balanceText.setText(format(balance));

        TextView frozenLabel = findViewById(R.id.frozen_money_label_id);
        frozenLabel.setText(String.format(getResources().getString(R.string.frozenMoney), CURRENCY));

        TextView frozenText = findViewById(R.id.frozen_money_id);
        frozenText.setText(format(frozenMoney));

        View.OnClickListener addressToClipboardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wallet.getAddress() == null) return;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if(clipboard == null) return;

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
                if(TextUtils.isEmpty(addressToSendEditor.getText())) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.sendPrompt));
                builder.setPositiveButton(getResources().getString(R.string.refund),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                refund(profile, addressToSendEditor.getText().toString());
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
