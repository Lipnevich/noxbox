package live.noxbox.pages;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import live.noxbox.MapActivity;
import live.noxbox.R;

import static java.util.Collections.singletonList;

public class AuthActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 11011;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        login();
        setContentView(R.layout.activity_auth);
        ((CheckBox) findViewById(R.id.checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    ((TextView) findViewById(R.id.textGoogleAuth)).setTextColor(getResources().getColor(R.color.secondary));
                    ((TextView) findViewById(R.id.textPhoneAuth)).setTextColor(getResources().getColor(R.color.secondary));
                } else {
                    ((TextView) findViewById(R.id.textGoogleAuth)).setTextColor(getResources().getColor(R.color.google_text));
                    ((TextView) findViewById(R.id.textPhoneAuth)).setTextColor(getResources().getColor(R.color.google_text));
                }

            }

        });
        createMultipleLinks((TextView) findViewById(R.id.agreementView));
        findViewById(R.id.googleAuth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuth(new AuthUI.IdpConfig.GoogleBuilder());
            }
        });
        findViewById(R.id.phoneAuth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuth(new AuthUI.IdpConfig.PhoneBuilder());
            }
        });
    }

    private void startAuth(AuthUI.IdpConfig.Builder provider) {
        if (NetworkReceiver.isOnline(this) && ((CheckBox) findViewById(R.id.checkbox)).isChecked()) {
            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(singletonList(provider.build()))
                    .build();
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        login();
    }

    private void login() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());
            startActivity(new Intent(this, MapActivity.class));
        }
    }

    private void createMultipleLinks(TextView textView) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                getResources().getString(R.string.iAgreeToThe).concat(" "));
        spanTxt.append(getResources().getString(R.string.termOfServices));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLink(getResources().getString(R.string.termsOfServiceLink));
            }
        }, spanTxt.length() - getResources().getString(R.string.termOfServices).length(), spanTxt.length(), 0);
        spanTxt.append(" ".concat(getResources().getString(R.string.and).concat(" ")));
        spanTxt.append(getResources().getString(R.string.privacyPolicy));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLink(getResources().getString(R.string.privacyPolicyLink));
            }
        }, spanTxt.length() - getResources().getString(R.string.privacyPolicy).length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

}
