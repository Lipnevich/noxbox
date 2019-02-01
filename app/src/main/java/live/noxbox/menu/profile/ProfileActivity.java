package live.noxbox.menu.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.activities.contract.NoxboxTypeListFragment;
import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.tools.FacePartsDetection;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.ProgressDialogManager;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.CAMERA_PERMISSION_REQUEST_CODE;
import static live.noxbox.Constants.REQUEST_IMAGE_CAPTURE;
import static live.noxbox.activities.contract.NoxboxTypeListFragment.PROFILE_CODE;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.ImageManager.createCircleImageFromBitmap;
import static live.noxbox.tools.ImageManager.createCircleProfilePhotoFromUrl;
import static live.noxbox.tools.ImageManager.getBitmap;

public class ProfileActivity extends BaseActivity {

    public static final int CODE = 1006;
    public static final int SELECT_IMAGE = 1007;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(ProfileActivity.class.getName(), profile -> draw(profile));

        findViewById(R.id.homeButton).setOnClickListener(v ->
                Router.finishActivity(ProfileActivity.this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
        AppCache.fireProfile();
    }

    private void draw(final Profile profile) {
        drawEditPhoto(profile);
        drawEditName(profile);
        drawEditHost(profile);
        drawPortfolioEditingMenu(profile);
        drawMenuAddingPerformer(profile);
    }

    private void drawEditPhoto(final Profile profile) {
        View.OnClickListener listener = view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.selectPhoto)), SELECT_IMAGE);
        };

        View.OnClickListener cameraListener = view -> {
            if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        };

        createCircleProfilePhotoFromUrl(this, profile.getPhoto(), (ImageView) findViewById(R.id.profilePhoto));

        findViewById(R.id.editPhoto).setOnClickListener(listener);
        findViewById(R.id.camera).setOnClickListener(cameraListener);
        ImageView profilePhoto = findViewById(R.id.profilePhoto);
        profilePhoto.setOnClickListener(listener);

        checkPhotoAcceptance(profile);
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        Router.startActivityForResult(this, intent, REQUEST_IMAGE_CAPTURE);
    }

    private void drawEditName(final Profile profile) {
        final EditText name = findViewById(R.id.name);
        name.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);

        findViewById(R.id.editName).setOnClickListener(view -> {
            name.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
        });

        if (!Strings.isNullOrEmpty(profile.getName())) {
            name.setText(profile.getName());
        }

        final TextInputLayout inputLayout = findViewById(R.id.textInputLayout);
        if (profile.getName() == null || profile.getName().length() < 1) {
            inputLayout.setErrorEnabled(true);
            inputLayout.setError("* " + getString(R.string.userNameWasNotVerified));
        }
        name.setEnabled(true);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newName = String.valueOf(s);
                if(profile.getCurrent() != null && profile.getNoxboxId() != null && !isNullOrZero(profile.getCurrent().getTimeCreated())){
                    inputLayout.requestFocus();
                    inputLayout.setFocusable(true);
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("* " + getString(R.string.messageCannotChangeName));
                    return;
                }
                if (!Strings.nullToEmpty(profile.getName()).equals(newName)) {
                    profile.setName(newName);
                }

                if (s.length() == 0) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("* " + getString(R.string.userNameWasNotVerified));
                } else {
                    inputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void drawEditHost(final Profile profile) {
        findViewById(R.id.switchHost).setVisibility(View.VISIBLE);
        ((Switch) findViewById(R.id.switchHost)).setChecked(profile.getHost());
        setHostStatus(profile.getHost(), profile);

        findViewById(R.id.hostLayout).setOnClickListener(view -> {
            if (profile.getHost()) {
                setHostStatus(false, profile);
            } else {
                setHostStatus(true, profile);
            }
        });
        ((Switch) findViewById(R.id.switchHost))
                .setOnCheckedChangeListener((buttonView, isChecked) ->
                        setHostStatus(isChecked, profile));
    }

    private void drawPortfolioEditingMenu(final Profile profile) {

        List<NoxboxType> typeList = new ArrayList<>();
        for (NoxboxType type : NoxboxType.values()) {
            if (profile.getPortfolio().get(type.name()) != null && profile.getPortfolio().get(type.name()).getTimeCreated() != 0) {
                typeList.add(type);
            }
        }
        if (typeList.size() >= 1) {
            findViewById(R.id.serviceNotProvidedLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.serviceProvidedText).setVisibility(View.VISIBLE);
            RecyclerView recyclerView = findViewById(R.id.noxboxTypeList);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new PortfolioNoxboxTypeAdapter(typeList, ProfileActivity.this));
        } else {
            findViewById(R.id.noxboxTypeList).setVisibility(View.GONE);
            findViewById(R.id.serviceNotProvidedLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.serviceProvidedText).setVisibility(View.GONE);
        }
    }

    private void drawMenuAddingPerformer(final Profile profile) {
        if (profile.getPortfolio().values().size() == NoxboxType.values().length) {
            findViewById(R.id.addLayout).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.addLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.addLayout).setOnClickListener(v -> {
            DialogFragment dialog = new NoxboxTypeListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("key", PROFILE_CODE);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), NoxboxTypeListFragment.TAG);
        });
    }

    private void checkPhotoAcceptance(Profile profile) {
        ImageView profilePhoto = findViewById(R.id.profilePhoto);

        if (!profile.getAcceptance().isAccepted()) {
            ((TextView) findViewById(R.id.invalidPhotoText)).setText("* " + getString(R.string.photoInvalidContent, getString(profile.getAcceptance().getInvalidAcceptance().getContent())));
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) profilePhoto.getLayoutParams();
            params.setMargins(0, 16, 0, 0); //substitute parameters for left, top, right, bottom
            profilePhoto.setLayoutParams(params);
            findViewById(R.id.invalidPhotoText).setVisibility(View.VISIBLE);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) profilePhoto.getLayoutParams();
            params.setMargins(0, 16, 0, 16); //substitute parameters for left, top, right, bottom
            profilePhoto.setLayoutParams(params);
            findViewById(R.id.invalidPhotoText).setVisibility(View.INVISIBLE);
        }
    }

    private void setHostStatus(boolean isChecked, Profile profile) {
        if (isChecked) {
            profile.setHost(isChecked);
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.haveHost);
        } else {
            profile.setHost(isChecked);
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.notHaveHost);
        }
        ((Switch) findViewById(R.id.switchHost)).setChecked(profile.getHost());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
            }
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        AppCache.readProfile(profile -> {
            if (requestCode == SELECT_IMAGE
                    && resultCode == Activity.RESULT_OK
                    && data != null && data.getData() != null) {
                ProgressDialogManager.showProgress(ProfileActivity.this, "Фотография проходит проверку");

                Map<String, String> notificationData = new HashMap<>();
                notificationData.put("type", NotificationType.photoValidationProgress.name());
                NotificationFactory.buildNotification(ProfileActivity.this.getApplicationContext(), null, notificationData).show();

                getBitmap(ProfileActivity.this, data.getData(), bitmap ->
                        FacePartsDetection.execute(bitmap, profile, ProfileActivity.this, checking -> {
                            ImageManager.uploadPhoto(ProfileActivity.this, profile, checking);
                            checkPhotoAcceptance(profile);
                        }));

                createCircleProfilePhotoFromUrl(ProfileActivity.this, data.getData().toString(), findViewById(R.id.profilePhoto));


            } else if (requestCode == TravelModeListActivity.CODE) {
                draw(profile);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                ProgressDialogManager.showProgress(ProfileActivity.this, "Фотография проходит проверку");

                Map<String, String> notificationData = new HashMap<>();
                notificationData.put("type", NotificationType.photoValidationProgress.name());
                NotificationFactory.buildNotification(ProfileActivity.this.getApplicationContext(), null, notificationData).show();

                Bundle photoData = data.getExtras();
                Bitmap photoBitmap = (Bitmap) photoData.get("data");

                getBitmap(ProfileActivity.this, photoBitmap, bitmap ->
                        FacePartsDetection.execute(bitmap, profile, ProfileActivity.this, checking -> {
                            ImageManager.uploadPhoto(ProfileActivity.this, profile, checking);
                            checkPhotoAcceptance(profile);
                        }));

                createCircleImageFromBitmap(ProfileActivity.this, photoBitmap, findViewById(R.id.profilePhoto));

            }

        });
    }

}
