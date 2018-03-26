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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.RequestType;
import by.nicolay.lipnevich.noxbox.pages.HistoryPage;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.IntentAndKey;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
import static by.nicolay.lipnevich.noxbox.tools.PageCodes.HISTORY;
import static by.nicolay.lipnevich.noxbox.tools.PageCodes.WALLET;

public abstract class ProfileActivity extends AuthActivity {

    @Override
    protected void processProfile(Profile profile) {
        createMenu();

        if(tryGetNoxboxInProgress() != null) {
            processNoxbox(tryGetNoxboxInProgress());
        } else if(calculateRating() >= MIN_RATE) {
            prepareForIteration();
        } else {
            popup("Low rate!");
        }

        if(new BigDecimal(Firebase.getWallet().getBalance()).compareTo(BigDecimal.ZERO) <= 0) {
            Firebase.sendRequest(new Request().setType(RequestType.balance));
        }

        listenMessages();
    }
    private double MIN_RATE = 4.5;

    protected Drawer menu;

    private void listenMessages() {
        Firebase.listenMessages(new Task() {
            @Override
            public void execute(Object object) {
                processMessage((Message)object);
            }
        });
    }

    private Double calculateRating() {
        // likes more then 90%
        Rating rating = Firebase.getRating().getReceived();

        if(rating == null || (rating.getLikes().equals(0l)
                && rating.getDislikes().equals(0l))) return 5.0;

        if(rating.getLikes() < 10 & rating.getDislikes().equals(1l)) return 4.5;
        if(rating.getLikes().equals(0l) && rating.getDislikes() > 1) return 0.0;

        return (double) (rating.getLikes() * 5) / (rating.getLikes() + rating.getDislikes());
    }

    protected void createMenu() {
        makeProfileImageRounded();

        ProfileDrawerItem account = new ProfileDrawerItem()
                .withName(getProfile().getName())
                .withEmail(String.format( "%.2f", calculateRating()) + " \u2605")
                ;
        if(getProfile().getPhoto() == null) {
            account.withIcon(ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.com_facebook_profile_picture_blank_square));
        } else {
            account.withIcon(getProfile().getPhoto());
        }


        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.primary)
                .withTextColorRes(R.color.secondary)
                .withProfileImagesClickable(false)
                .withSelectionListEnabledForSingleProfile(false)
                .withAccountHeader(R.layout.material_drawer_header)
                .addProfiles(account)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        menu = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .withAccountHeader(header)
                .addDrawerItems(convertToItems(getMenu()))
                .build();

        menu.getDrawerLayout().setFitsSystemWindows(true);

        ImageView menuImage = (ImageView) findViewById(R.id.menuImage);
        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.openDrawer();
            }
        });
    }

    private void makeProfileImageRounded() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(getApplicationContext()).load(uri)
                        .apply(RequestOptions.placeholderOf(placeholder))
                        .into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.with(getApplicationContext()).clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return ContextCompat.getDrawable(getApplicationContext(), R.drawable.com_facebook_profile_picture_blank_square);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                return placeholder(ctx);
            }
        });
    }

    private IDrawerItem[] convertToItems(Map<String, IntentAndKey> menu) {
        List<IDrawerItem> items = new ArrayList<>();
        for(final Map.Entry<String, IntentAndKey> entry : menu.entrySet()) {
            PrimaryDrawerItem item = new PrimaryDrawerItem()
                    .withIdentifier(entry.getKey().hashCode())
                    .withName(entry.getKey())
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            startActivityForResult(entry.getValue().getIntent(), entry.getValue().getKey());
                            return true;
                    }})
                    .withTextColorRes(R.color.primary);

            items.add(item);
        }

        PrimaryDrawerItem logout = new PrimaryDrawerItem()
                .withIdentifier(999)
                .withName(getResources().getString(R.string.logout))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this,
                                R.style.NoxboxAlertDialogStyle);
                        builder.setTitle(getResources().getString(R.string.logoutPrompt));
                        builder.setPositiveButton(getResources().getString(R.string.logout),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AuthUI.getInstance()
                                                .signOut(ProfileActivity.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                        return true;
                    }})
                .withTextColorRes(R.color.primary);
        items.add(logout);

        return items.toArray(new IDrawerItem[menu.size()]);
    }

    @Override
    protected SortedMap<String, IntentAndKey> getMenu() {
        TreeMap<String, IntentAndKey> menu = new TreeMap<>();
        menu.put(getString(R.string.history), new IntentAndKey()
                .setIntent(new Intent(getApplicationContext(), HistoryPage.class))
                .setKey(HISTORY.getCode()));
//        map.put(getString(R.string.profile), new Intent(getApplicationContext(), ProfilePage.class));
//        map.put(getString(R.string.help), new Intent(getApplicationContext(), MyCarPage.class));
//        map.put(getString(R.string.settings), new Intent(getApplicationContext(), MyCarPage.class));
        return menu;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == WALLET.getCode() && Firebase.getProfile() != null) {
            listenMessages();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

}
