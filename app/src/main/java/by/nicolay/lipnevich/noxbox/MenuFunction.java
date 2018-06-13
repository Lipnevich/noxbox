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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import by.nicolay.lipnevich.noxbox.model.Acceptance;
import by.nicolay.lipnevich.noxbox.model.EventType;
import by.nicolay.lipnevich.noxbox.model.IntentAndKey;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.pages.AuthPage;
import by.nicolay.lipnevich.noxbox.pages.HistoryPage;
import by.nicolay.lipnevich.noxbox.pages.WalletPage;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.getProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.readProfile;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.updateProfile;

public abstract class MenuFunction extends AppCompatActivity {

    private float MIN_RATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MIN_RATE = getResources().getFraction(R.fraction.min_allowed_probability, 1, 1);

        readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                processProfile(profile);
            }
        });
    }

    protected void popup(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void processProfile(Profile profile) {
        createMenu();

        if(calculateRating() <= MIN_RATE) {
            popup("Low rate!");
        }

        if(new BigDecimal(Firebase.getWallet().getBalance()).compareTo(BigDecimal.ZERO) <= 0) {
            Firebase.sendRequest(new Request().setType(EventType.balance));
        }

        ImageView check = findViewById(R.id.check);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAcceptance();
            }
        });
        check.setVisibility(View.VISIBLE);

//        listenEvents();
    }

    protected Drawer menu;

    protected void checkAcceptance() {
        // TODO (nli) replace it with profile activity launch
        float minProb = getResources().getFraction(R.fraction.min_allowed_probability, 1, 1);
        Acceptance acceptance = getProfile().getAcceptance();
        if(acceptance.getExpired()) {
            popup("Please wait. Your data check still in progress...");
        } else if(!acceptance.isAccepted(minProb)) {
            String warning = "";
            if(acceptance.getCorrectNameProbability() < minProb) {
                warning += "Incorrect name\n";
            }
            if(acceptance.getFailToRecognizeFace()) {
                warning += "Fail to recognize face\n";
            } else {
                if (acceptance.getSmileProbability() < minProb) {
                    warning += "No smile on photo\n";
                }
                if (acceptance.getLeftEyeOpenProbability() < minProb) {
                    warning += "Closed left eye\n";
                }
                if (acceptance.getRightEyeOpenProbability() < minProb) {
                    warning += "Closed right eye\n";
                }
            }
            warning += "Please update your data on your Google account";
            popup(warning);
        } else {
            popup("Your name and photo are just wonderful!");
        }
    }

    private Double calculateRating() {
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
                .withEmail(String.format( "%.2f", calculateRating()) + " \u2605");
        if(getProfile().getPhoto() == null) {
            account.withIcon(ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.profile_picture_blank));
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

        SecondaryDrawerItem version = new SecondaryDrawerItem()
                .withName("Version " + BuildConfig.VERSION_NAME)
                .withEnabled(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        menu = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .withAccountHeader(header)
                .addDrawerItems(convertToItems(getMenu()))
                .addStickyDrawerItems(version)
                .withStickyFooterShadow(false)
                .build();

        menu.getDrawerLayout().setFitsSystemWindows(true);

        ImageView menuImage = findViewById(R.id.menu);
        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.openDrawer();
            }
        });
        menuImage.setVisibility(View.VISIBLE);
    }

    private void makeProfileImageRounded() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(final ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(getApplicationContext()).asBitmap().load(uri)
                        .apply(RequestOptions.placeholderOf(placeholder).circleCrop())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<?
                                                                super Bitmap> transition) {
                                imageView.setImageBitmap(resource);
                                checkPhoto(resource);
                            }
                        });
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.with(getApplicationContext()).clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_picture_blank);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                return placeholder(ctx);
            }
        });
    }

    private void checkPhoto(Bitmap photo) {
        if(!getProfile().getAcceptance().getExpired()) return;

        if(getProfile().getPhoto() == null) {
            getProfile().getAcceptance().setFailToRecognizeFace(true);
            getProfile().getAcceptance().setExpired(false);
            updateProfile(getProfile());
            return;
        }

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.6f)
                        .setTrackingEnabled(false)
                        .build());

        detector.detectInImage(FirebaseVisionImage.fromBitmap(photo))
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                getProfile().getAcceptance().setExpired(false);
                                if(faces.size() != 1) {
                                    getProfile().getAcceptance().setFailToRecognizeFace(true);
                                } else {
                                    FirebaseVisionFace face = faces.get(0);
                                    getProfile().getAcceptance().setSmileProbability(face.getSmilingProbability());
                                    getProfile().getAcceptance().setRightEyeOpenProbability(face.getRightEyeOpenProbability());
                                    getProfile().getAcceptance().setLeftEyeOpenProbability(face.getLeftEyeOpenProbability());
                                }
                                updateProfile(getProfile());
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                getProfile().getAcceptance().setExpired(false);
                                getProfile().getAcceptance().setFailToRecognizeFace(true);
                                updateProfile(getProfile());
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(MenuFunction.this,
                                R.style.NoxboxAlertDialogStyle);
                        builder.setTitle(getResources().getString(R.string.logoutPrompt));
                        builder.setPositiveButton(getResources().getString(R.string.logout),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AuthUI.getInstance()
                                                .signOut(MenuFunction.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                        // TODO (nli) start activity for result
                                                        startActivity(new Intent(MenuFunction.this, AuthPage.class));
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

    protected SortedMap<String, IntentAndKey> getMenu() {
        TreeMap<String, IntentAndKey> menu = new TreeMap<>();
        menu.put(getString(R.string.history), new IntentAndKey()
                .setIntent(new Intent(getApplicationContext(), HistoryPage.class))
                .setKey(HistoryPage.CODE));
        menu.put(getString(R.string.wallet), new IntentAndKey()
                .setIntent(new Intent(getApplicationContext(), WalletPage.class))
                .setKey(WalletPage.CODE));
        return menu;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == WalletPage.CODE && Firebase.getProfile() != null) {
            draw();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract void draw();

}
