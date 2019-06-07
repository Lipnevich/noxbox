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
package live.noxbox.menu;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import live.noxbox.R;
import live.noxbox.activities.AuthActivity;
import live.noxbox.activities.BaseActivity;
import live.noxbox.database.AppCache;
import live.noxbox.menu.about.AboutApplicationActivity;
import live.noxbox.menu.history.HistoryActivity;
import live.noxbox.menu.profile.ProfileActivity;
import live.noxbox.menu.settings.MapSettingsActivity;
import live.noxbox.menu.wallet.WalletActivity;
import live.noxbox.model.Profile;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.Router;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.menu.history.HistoryActivity.KEY_COMPLETE;
import static live.noxbox.menu.history.HistoryActivity.KEY_PERFORMER_ID;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.DisplayMetricsConservations.getStatusBarHeight;

public abstract class MenuActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView photoView;
    private TextView nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        photoView = navigationView.getHeaderView(0).findViewById(R.id.photo);
        nameView = navigationView.getHeaderView(0).findViewById(R.id.name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        profile().init(FirebaseAuth.getInstance().getCurrentUser());
        AppCache.listenProfile(MenuActivity.class.getName(), profile -> draw(MenuActivity.this, profile));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        isInitiated = false;
    }

    protected void draw(final Activity activity, final Profile profile) {
        initializeNavigationHeader(activity, profile);
        drawNavigation(activity, profile);
    }

    private void drawNavigation(final Activity activity, final Profile profile) {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.menu).setOnClickListener(v -> {
            openNavigation(profile, activity);
        });
    }

    protected void openNavigation(Profile profile, Activity activity) {
        if (drawerLayout == null) return;
        drawerLayout.openDrawer(GravityCompat.START);
        initializeNavigationHeader(activity, profile);
    }

    private Boolean isInitiated = false;

    private void initializeNavigationHeader(final Activity activity, final Profile profile) {
        if (!isInitiated) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPx(96), dpToPx(96));
            layoutParams.setMargins(dpToPx(16), dpToPx(32) + getStatusBarHeight(getApplicationContext()), 0, 0);
            photoView.setLayoutParams(layoutParams);


            if (profile.getPhoto() == null) {
                ImageManager.createCircleImageFromBitmap(getApplicationContext(), BitmapFactory.decodeResource(getResources(), R.drawable.human_profile), photoView);
            } else {
                ImageManager.createCircleProfilePhotoFromUrl(getApplicationContext(), profile.getPhoto(), photoView);
            }
            if (!Strings.isNullOrEmpty(profile.getName())) {
                nameView.setText(profile.getName());
            }

            //((TextView) findViewById(R.id.rating)).setText(String.valueOf(profile.ratingToPercentage()).concat(" %"));

            photoView.setOnClickListener(v -> {
                Router.startActivityForResult(activity, ProfileActivity.class, ProfileActivity.CODE);
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }
                isInitiated = false;
            });
            isInitiated = true;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerLayout == null) return;
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_settings: {
                closeDrawer();
                Router.startActivityForResult(this, MapSettingsActivity.class, MapSettingsActivity.CODE);
                break;
            }
            case R.id.navigation_history: {
                closeDrawer();
                if (profile().getCurrent().getFinished()) {
                    Intent lastNoxboxIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                    lastNoxboxIntent.putExtra(KEY_COMPLETE, profile().getCurrent().getTimeCompleted());
                    lastNoxboxIntent.putExtra(KEY_PERFORMER_ID, profile().getCurrent().getId());
                    startActivityForResult(lastNoxboxIntent, HistoryActivity.CODE);
                } else {
                    Router.startActivityForResult(this, HistoryActivity.class, HistoryActivity.CODE);
                }
                break;
            }
            case R.id.navigation_profile: {
                closeDrawer();
                Router.startActivityForResult(this, ProfileActivity.class, ProfileActivity.CODE);
                break;
            }
            case R.id.navigation_wallet: {
                closeDrawer();
                Router.startActivityForResult(this, WalletActivity.class, WalletActivity.CODE);
                break;
            }
            case R.id.navigation_about_app: {
                closeDrawer();
                Router.startActivity(this, AboutApplicationActivity.class);
                break;
            }
            case R.id.navigation_logout: {
                closeDrawer();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) return true;

                AppCache.logout();
                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUser.getUid());

                FirebaseAuth.getInstance().signOut();
                // we need it for selecting new profile
                AuthUI.getInstance().signOut(this);
                startActivity(new Intent(getApplicationContext(), AuthActivity.class));
                Router.finishActivity(MenuActivity.this);
            }
        }
        return true;
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
        isInitiated = false;
    }


}
