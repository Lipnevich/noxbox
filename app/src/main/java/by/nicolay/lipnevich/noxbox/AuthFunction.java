/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package by.nicolay.lipnevich.noxbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.Event;
import by.nicolay.lipnevich.noxbox.model.IntentAndKey;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;
import io.fabric.sdk.android.Fabric;

public abstract class AuthFunction extends AppCompatActivity {

    private static final String TERMS_URL = "https://noxbox.io/TermsAndConditions.html";
    private static final String PRIVACY_URL = "https://noxbox.io/NoxBoxPrivacyPolicy.pdf";
    private static final int SIGN_IN_REQUEST_CODE = 10110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCrashReporting();
        login();
    }

    private void initCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    protected void login() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent login = AuthUI.getInstance().createSignInIntentBuilder()
                    .setTheme(R.style.LoginTheme)
                    .setLogo(R.drawable.noxbox)
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                    .setTosUrl(TERMS_URL)
                    .setPrivacyPolicyUrl(PRIVACY_URL)
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .build();

            startActivityForResult(login, SIGN_IN_REQUEST_CODE);
        } else {
            Firebase.readProfile(processProfileTask);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Firebase.readProfile(processProfileTask);
            } else {
                popup(getResources().getText(R.string.permissionRequired).toString());
            }
        }
    }

    private final Task processProfileTask = new Task<Profile>() {
        @Override
        public void execute(Profile profile) {
            processProfile(profile);
        }
    };

    protected void popup(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected abstract void processProfile(Profile profile);
    protected abstract void goOnline();
    protected abstract void processEvent(Event event);
    protected abstract void processNoxbox(Noxbox noxbox);
    protected abstract void prepareForIteration();
    protected abstract Map<String, IntentAndKey> getMenu();

}