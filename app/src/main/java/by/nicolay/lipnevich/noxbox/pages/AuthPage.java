package by.nicolay.lipnevich.noxbox.pages;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

import by.nicolay.lipnevich.noxbox.BuildConfig;
import by.nicolay.lipnevich.noxbox.MapActivity;
import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.tools.FragmentManager;
import io.fabric.sdk.android.Fabric;

import static by.nicolay.lipnevich.noxbox.tools.DebugMessage.popup;

public class AuthPage extends AppCompatActivity {

    private static final String TERMS_URL = "https://noxbox.io/TermsAndConditions.html";
    private static final String PRIVACY_URL = "https://noxbox.io/NoxBoxPrivacyPolicy.pdf";

    private static final int REQEUST_CODE = 11011;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCrashReporting();
        login();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_auth);
        ((CheckBox) findViewById(R.id.checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.googleAuth).setEnabled(isChecked);
                findViewById(R.id.phoneAuth).setEnabled(isChecked);
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
        if(isOnline()){
            Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setTheme(R.style.LoginTheme)
                    .setAvailableProviders(Arrays.asList(provider.build()))
                    .build();
            startActivityForResult(intent, REQEUST_CODE);
        }else{
            popup(this,"No internet");
            FragmentManager.createFragment(this, new WarningFragmemt(), R.id.messageContainer);
        }
    }

    private void initCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        login();
    }

    private void login() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(AuthPage.this, MapActivity.class));
        }
    }

    private void createMultipleLinks(TextView textView) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "I agree to the ");
        spanTxt.append("Term of services");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLink(TERMS_URL);
            }
        }, spanTxt.length() - "Term of services".length(), spanTxt.length(), 0);
        spanTxt.append(" and");
        spanTxt.append(" Privacy Policy");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLink(PRIVACY_URL);
            }
        }, spanTxt.length() - " Privacy Policy".length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
