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
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import live.noxbox.BaseActivity;
import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.filters.MapFiltersActivity;
import live.noxbox.model.Profile;
import live.noxbox.pages.AuthActivity;
import live.noxbox.profile.ProfileActivity;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

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
        AppCache.listenProfile(MenuActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(MenuActivity.this, profile);
            }
        });
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

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
                initializeNavigationHeader(activity, profile);
            }
        });
    }

    private Boolean isInitial = false;

    private void initializeNavigationHeader(final Activity activity, final Profile profile) {
        if (!isInitial) {
            ImageView profilePhoto = findViewById(R.id.photo);
            if (profile.getPhoto() == null) {
                ImageManager.createCircleImageFromBitmap(activity, BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture_blank), (profilePhoto));
            } else {
                ImageManager.createCircleImageFromUrl(activity, profile.getPhoto(), profilePhoto);
            }
            if (profile.getName() != null) {
                ((TextView) findViewById(R.id.name)).setText(profile.getName());
            }
            ((TextView) findViewById(R.id.rating)).setText(String.valueOf(profile.ratingToPercentage()).concat(" %"));
            ((TextView) findViewById(R.id.version)).setText(getResources().getString(R.string.version).concat(" ").concat(BuildConfig.VERSION_NAME));
            profilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.startActivityForResult(activity, ProfileActivity.class, ProfileActivity.CODE);
                }
            });
            isInitial = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == WalletActivity.CODE) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    draw(MenuActivity.this, profile);
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_filters: {
                Router.startActivityForResult(this, MapFiltersActivity.class, MapFiltersActivity.CODE);
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
            case R.id.navigation_logout: {
                AppCache.readProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            AppCache.logout();
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUser.getUid());
                        }
                        AuthUI.getInstance()
                                .signOut(MenuActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {

                                        startActivity(new Intent(getApplicationContext(), AuthActivity.class));
                                        finish();
                                    }
                                });
                    }
                });

            }
        }
        return true;
    }

}
