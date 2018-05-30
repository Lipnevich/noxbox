package by.nicolay.lipnevich.noxbox.pages;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Event;
import by.nicolay.lipnevich.noxbox.model.EventType;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.Wallet;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getWallet;
import static by.nicolay.lipnevich.noxbox.tools.MoneyFormatter.format;
import static by.nicolay.lipnevich.noxbox.tools.MoneyFormatter.scale;

public class WalletPage extends AppCompatActivity {

    public static final int CODE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallet);
        setTitle(R.string.wallet);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recalculateBalance();

        final EditText addressToSendEditor = findViewById(R.id.address_to_send_id);
        if(getProfile() != null && getProfile().getAddressToRefund() != null) {
            addressToSendEditor.setText(getProfile().getAddressToRefund());
        }

        Button sendButton = findViewById(R.id.send_button_id);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(addressToSendEditor.getText())) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletPage.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.sendPrompt));
                builder.setPositiveButton(getResources().getString(R.string.refund),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                refund(addressToSendEditor.getText().toString());
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
        });

        Firebase.listenTypeEvents(EventType.balance, new Task<Event>() {
            @Override
            public void execute(Event event) {
                Firebase.updateWallet(event.getWallet());
                recalculateBalance();
            }
        });
        Firebase.sendRequest(new Request().setType(EventType.balance));
    }

    private void refund(String address) {
        if(TextUtils.isEmpty(address) || getProfile() == null) return;

        if(!address.equals(getProfile().getAddressToRefund())) {
            Firebase.updateProfile(getProfile().setAddressToRefund(address));
        }

        Firebase.sendRequest(new Request()
                .setType(EventType.refund)
                .setAddress(getProfile().getAddressToRefund()));
    }

    private void recalculateBalance() {
        final Wallet wallet = getWallet();
        BigDecimal balance = wallet.getBalance() != null ? new BigDecimal(wallet.getBalance()) : BigDecimal.ZERO;
        BigDecimal frozenMoney = wallet.getFrozenMoney() != null ? new BigDecimal(wallet.getFrozenMoney()) : BigDecimal.ZERO;
        balance = scale(balance.subtract(frozenMoney));

        String cryptoCurrency = getResources().getString(R.string.crypto_currency);

        TextView balanceLabel = findViewById(R.id.balance_label_id);
        balanceLabel.setText(String.format(getResources().getString(R.string.balance), cryptoCurrency));

        TextView balanceText = findViewById(R.id.balance_id);
        balanceText.setText(format(balance));

        TextView frozenLabel = findViewById(R.id.frozen_money_label_id);
        frozenLabel.setText(String.format(getResources().getString(R.string.frozenMoney), cryptoCurrency));

        TextView frozenText = findViewById(R.id.frozen_money_id);
        frozenText.setText(format(frozenMoney));

        View.OnClickListener addressToClipboardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getWallet().getAddress() == null) return;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if(clipboard == null) return;

                clipboard.setPrimaryClip(ClipData.newPlainText("walletAddress", getWallet().getAddress()));
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.addressInClipBoard), Toast.LENGTH_SHORT).show();
            }
        };

        TextView walletAddress = findViewById(R.id.wallet_address_id);
        walletAddress.setText(getWallet().getAddress() != null ? getWallet().getAddress() : "Not created yet");
        walletAddress.setOnClickListener(addressToClipboardListener);

        ImageView copyToClipboard = findViewById(R.id.copy_to_clipboard_id);
        copyToClipboard.setVisibility(getWallet().getAddress() != null ? View.VISIBLE : View.INVISIBLE);
        copyToClipboard.setOnClickListener(addressToClipboardListener);

        Button sendButton = findViewById(R.id.send_button_id);
        sendButton.setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);

        EditText addressToSendEditor = findViewById(R.id.address_to_send_id);
        addressToSendEditor.setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
