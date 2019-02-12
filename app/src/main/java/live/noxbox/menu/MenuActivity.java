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
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import static live.noxbox.database.AppCache.isProfileReady;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.DisplayMetricsConservations.getStatusBarHeight;

public abstract class MenuActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(MenuActivity.class.getName(), profile -> draw(MenuActivity.this, profile));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
    }

    private void draw(final Activity activity, final Profile profile) {
        drawNavigation(activity, profile);
    }

    private void drawNavigation(final Activity activity, final Profile profile) {
        drawerLayout = findViewById(R.id.drawerLayout);

        if (!isProfileReady()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            return;
        }

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                initializeNavigationHeader(activity, profile);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });


        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.menu).setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
            initializeNavigationHeader(activity, profile);
        });
    }

    private Boolean isInitial = false;

    private void initializeNavigationHeader(final Activity activity, final Profile profile) {
        if (!isInitial) {
            ImageView profilePhoto = findViewById(R.id.photo);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPx(96), dpToPx(96));
            layoutParams.setMargins(dpToPx(16), dpToPx(32) + getStatusBarHeight(getApplicationContext()), 0, 0);
            profilePhoto.setLayoutParams(layoutParams);


            if (profile.getPhoto() == null) {
                ImageManager.createCircleImageFromBitmap(activity, BitmapFactory.decodeResource(getResources(), R.drawable.human_profile), (profilePhoto));
            } else {
                ImageManager.createCircleProfilePhotoFromUrl(activity, profile.getPhoto(), profilePhoto);
            }
            if (!Strings.isNullOrEmpty(profile.getName())) {
                ((TextView) findViewById(R.id.name)).setText(profile.getName());
            }

            //((TextView) findViewById(R.id.rating)).setText(String.valueOf(profile.ratingToPercentage()).concat(" %"));

            profilePhoto.setOnClickListener(v -> {
                Router.startActivityForResult(activity, ProfileActivity.class, ProfileActivity.CODE);
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }
                isInitial = false;
            });
            isInitial = true;
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
                Router.startActivityForResult(this, MapSettingsActivity.class, MapSettingsActivity.CODE);
                break;
            }
            case R.id.navigation_history: {
                Router.startActivityForResult(this, HistoryActivity.class, HistoryActivity.CODE);
                break;
            }
            case R.id.navigation_profile: {
                Router.startActivityForResult(this, ProfileActivity.class, ProfileActivity.CODE);
                break;
            }
            case R.id.navigation_wallet: {
                Router.startActivityForResult(this, WalletActivity.class, WalletActivity.CODE);
                break;
            }
            case R.id.navigation_about_app: {
                Router.startActivity(this, AboutApplicationActivity.class);
                break;
            }
            case R.id.navigation_logout: {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) return true;

                AppCache.logout();
                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUser.getUid());

                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(getApplicationContext(), AuthActivity.class));
                Router.finishActivity(MenuActivity.this);
            }
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
        isInitial = false;
        return true;
    }


}
