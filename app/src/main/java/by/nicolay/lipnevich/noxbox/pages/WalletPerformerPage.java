package by.nicolay.lipnevich.noxbox.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.MessageType;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.RequestType;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.model.Wallet;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.getWallet;
import static by.nicolay.lipnevich.noxbox.tools.Numbers.format;
import static by.nicolay.lipnevich.noxbox.tools.Numbers.scale;

public class WalletPerformerPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallet_performer);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletPerformerPage.this, R.style.NoxboxAlertDialogStyle);
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

        Firebase.listenTypeMessages(MessageType.balanceUpdated, new Task<Message>() {
            @Override
            public void execute(Message message) {
                Firebase.updateWallet(message.getWallet());
                recalculateBalance();
            }
        });
        Firebase.sendRequest(new Request().setType(RequestType.balance).setRole(UserType.performer));
    }

    private void refund(String address) {
        if(TextUtils.isEmpty(address) || getProfile() == null) return;

        if(!address.equals(getProfile().getAddressToRefund())) {
            Firebase.updateProfile(getProfile().setAddressToRefund(address));
        }

        Firebase.sendRequest(new Request()
                .setType(RequestType.refund)
                .setToAddress(getProfile().getAddressToRefund()));
    }

    private void recalculateBalance() {
        final Wallet wallet = getWallet();
        BigDecimal price = Firebase.getPrice();
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

        TextView priceLabel = findViewById(R.id.current_reward_label_id);
        priceLabel.setText(String.format(getResources().getString(R.string.current_reward), cryptoCurrency));

        TextView priceText = findViewById(R.id.current_reward_id);
        priceText.setText(price == null ? "..." : format(price));

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
