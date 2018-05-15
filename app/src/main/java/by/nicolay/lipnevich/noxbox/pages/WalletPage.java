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
import java.math.RoundingMode;

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

public class WalletPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallet);
        setTitle(R.string.wallet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recalculateBalance();

        final EditText addressToRefundEditor = findViewById(R.id.address_to_refund_id);
        if(getProfile().getAddressToRefund() != null) {
            addressToRefundEditor.setText(getProfile().getAddressToRefund());
        }

        Button refundButton = findViewById(R.id.refund_button_id);
        refundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(addressToRefundEditor.getText())) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletPage.this, R.style.NoxboxAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.refundPrompt));
                builder.setPositiveButton(getResources().getString(R.string.refund),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                refund(addressToRefundEditor.getText().toString());
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
        Firebase.sendRequest(new Request().setType(RequestType.balance).setRole(UserType.payer));
    }

    private void refund(String address) {
        if(TextUtils.isEmpty(address)) return;

        if(!address.equals(getProfile().getAddressToRefund())) {
            Firebase.updateProfile(getProfile().setAddressToRefund(address));
        }

        Firebase.sendRequest(new Request()
                .setType(RequestType.refund)
                .setToAddress(getProfile().getAddressToRefund()));

        Firebase.getWallet().setBalance("0");

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

        TextView priceLabel = findViewById(R.id.price_label_id);
        priceLabel.setText(String.format(getResources().getString(R.string.price), cryptoCurrency));

        TextView priceView = findViewById(R.id.price_id);
        priceView.setText(format(price));

        TextView servicesDescription = findViewById(R.id.services_description_id);
        BigDecimal numberOfServices = balance.divide(price, 0, RoundingMode.DOWN);
        TextView numberOfNoxboxes = findViewById(R.id.number_of_noxboxes);
        if(numberOfServices.compareTo(BigDecimal.ONE) < 0) {
            numberOfNoxboxes.setText("");
            servicesDescription.setText(String.format(getResources().getString(R.string.serviceUnavailable),
                    format(price.subtract(balance)) + " " + cryptoCurrency));
        } else if (numberOfServices.compareTo(BigDecimal.ONE) == 0) {
            numberOfNoxboxes.setText("1");
            servicesDescription.setText(getResources().getString(R.string.serviceAvailable));
        } else {
            numberOfNoxboxes.setText(format(numberOfServices, 0));
            servicesDescription.setText(getResources().getString(R.string.servicesAvailable));
        }

        View.OnClickListener addressToClipboardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getWallet().getAddress() == null) return;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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

        Button refundButton = findViewById(R.id.refund_button_id);
        refundButton.setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);

        EditText addressToRefundEditor = findViewById(R.id.address_to_refund_id);
        addressToRefundEditor.setVisibility(balance.compareTo(BigDecimal.ZERO) == 0 ? View.INVISIBLE : View.VISIBLE);
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
