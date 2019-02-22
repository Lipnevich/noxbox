package live.noxbox.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder;
import com.firebase.ui.auth.AuthUI.IdpConfig.PhoneBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.menu.about.tutorial.TutorialActivity;
import live.noxbox.services.NetworkReceiver;

import static java.util.Collections.singletonList;
import static live.noxbox.Constants.FIRST_RUN_KEY;

public class AuthActivity extends BaseActivity {

    private static final int REQUEST_CODE = 11011;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        login();
        setContentView(R.layout.activity_auth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((CheckBox) findViewById(R.id.checkbox)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                colorText(R.id.textGoogleAuth, R.color.secondary);
                colorText(R.id.textPhoneAuth, R.color.secondary);
                findViewById(R.id.googleAuth).setVisibility(View.VISIBLE);
                findViewById(R.id.phoneAuth).setVisibility(View.VISIBLE);
            } else {
                colorText(R.id.textGoogleAuth, R.color.google_text);
                colorText(R.id.textPhoneAuth, R.color.google_text);
                findViewById(R.id.googleAuth).setVisibility(View.INVISIBLE);
                findViewById(R.id.phoneAuth).setVisibility(View.INVISIBLE);
            }

        });
        drawAgreement();
        findViewById(R.id.googleAuth).setOnClickListener(authentificate(new GoogleBuilder()));
        findViewById(R.id.phoneAuth).setOnClickListener(authentificate(new PhoneBuilder()));
    }


    private void colorText(int textView, int color) {
        ((TextView) findViewById(textView)).setTextColor(getResources().getColor(color));
    }

    private View.OnClickListener authentificate(final AuthUI.IdpConfig.Builder provider) {
        return v -> {
            if (NetworkReceiver.isOnline(AuthActivity.this) && ((CheckBox) findViewById(R.id.checkbox)).isChecked()) {
                Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(singletonList(provider.build()))
                        .build();
                startActivityForResult(intent, REQUEST_CODE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE)
            login();
    }

    private void login() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            AppCache.profile().init(user);
            if (isFirstRun(true)) {
                startActivity(new Intent(this, TutorialActivity.class).putExtra(FIRST_RUN_KEY, FIRST_RUN_KEY));
            } else {
                startActivity(new Intent(this, MapActivity.class));
            }
            finish();
        }

    }

    private void drawAgreement() {
        String termOfServices = getString(R.string.authRules);
        String privacyPolicy = getString(R.string.authPrivacyPolicy);

        SpannableStringBuilder agreement = new SpannableStringBuilder(getString(R.string.agreement,
                termOfServices, privacyPolicy));
        createLink(agreement, termOfServices, R.string.rulesLink);
        createLink(agreement, privacyPolicy, R.string.privacyPolicyLink);

        TextView view = findViewById(R.id.agreementView);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(agreement, TextView.BufferType.SPANNABLE);
    }

    private void createLink(SpannableStringBuilder links, String text, final int url) {
        int start = links.toString().indexOf(text);
        links.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(url))));
            }
        }, start, start + text.length(), 0);

    }

}
