package live.noxbox.profile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.FirestoreReference;
import live.noxbox.tools.Task;

import static live.noxbox.tools.ImageManager.createCircleImageFromUrl;

public class ProfileActivity extends FragmentActivity {

    public static final int CODE = 1006;
    public static final int SELECT_IMAGE = 1007;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ProfileStorage.listenProfile(ProfileActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });

        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }


    private void draw(final Profile profile) {
        findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawEditable(profile);

            }
        });
        ((ImageView) findViewById(R.id.editProfile)).setImageResource(R.drawable.edit);
        drawPhoto(profile);
        drawName(profile);
        drawTravelMode(profile);
        drawHost(profile);
        drawCertificates(profile);
        drawWorkSamples(profile);
    }


    private void drawPhoto(final Profile profile) {
        findViewById(R.id.profileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (profile.getPhoto() != null) {
            createCircleImageFromUrl(this, profile.getPhoto(), (ImageView) findViewById(R.id.profileImage));
            return;
        }
        ((ImageView)findViewById(R.id.profileImage)).setImageResource(R.drawable.unknown_profile);
    }

    private void drawName(Profile profile) {
        ((TextView) findViewById(R.id.nameInHead)).setText(profile.getName());
        ((EditText) findViewById(R.id.editName)).setText(profile.getName());
        ((EditText) findViewById(R.id.editName)).setEnabled(false);
    }

    private void drawTravelMode(final Profile profile) {
        setTravelModeStatus(profile);
        findViewById(R.id.travelModeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.editTravelMode).setVisibility(View.GONE);
    }

    private void drawHost(final Profile profile) {

        findViewById(R.id.hostLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if (profile.getTravelMode() == TravelMode.none || profile.getHost()) {
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.yes);
        } else {
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.no);
        }
        findViewById(R.id.switchHost).setVisibility(View.GONE);
    }

    private void drawCertificates(final Profile profile) {
        List<String> certificatesList = new ArrayList<>();
        certificatesList.add("https://i.pinimg.com/736x/1d/ba/a1/1dbaa1fb5b2f64e54010cf6aae72b8b1.jpg");
        certificatesList.add("http://4u-professional.com/assets/images/sert/gel-lak.jpg");
        certificatesList.add("https://www.hallyuuk.com/wp-content/uploads/2018/06/reiki-master-certificate-template-inspirational-reiki-certificate-templates-idealstalist-of-reiki-master-certificate-template.jpg");
        certificatesList.add("http://www.childminder.ng/blog_pics/1479134810.jpg");
        certificatesList.add(" ");
        RecyclerView myList = (RecyclerView) findViewById(R.id.certificatesList);
        myList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myList.setAdapter(new CertificatesAdapter(certificatesList, this));
    }

    private void drawWorkSamples(final Profile profile) {
        List<String> workSampleList = new ArrayList<>();
        workSampleList.add("http://coolmanicure.com/media/k2/items/cache/stilnyy_manikur_so_strazami_XL.jpg");
        workSampleList.add("http://rosdesign.com/design_materials3/img_materials3/kopf/kopf1.jpg");
        workSampleList.add("http://vmirevolos.ru/wp-content/uploads/2015/12/61.jpg");
        workSampleList.add(" ");
        RecyclerView myList = (RecyclerView) findViewById(R.id.workSampleList);
        myList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myList.setAdapter(new CertificatesAdapter(workSampleList, this));
    }


    private void drawEditable(final Profile profile) {
        findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw(profile);

            }
        });
        ((ImageView) findViewById(R.id.editProfile)).setImageResource(R.drawable.yes);
        drawEditPhoto(profile);
        drawEditName(profile);
        drawEditTravelMode(profile);
        drawEditHost(profile);
    }

    private void drawEditPhoto(final Profile profile) {
        createCircleImageFromUrl(this, profile.getPhoto(), (ImageView) findViewById(R.id.profileImage));
        findViewById(R.id.profileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
            }
        });

    }

    private void drawEditName(final Profile profile) {
        ((EditText) findViewById(R.id.editName)).setEnabled(true);
        ((EditText) findViewById(R.id.editName)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profile.setName(String.valueOf(s));
                ((TextView) findViewById(R.id.nameInHead)).setText(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void drawEditTravelMode(final Profile profile) {
        setTravelModeStatus(profile);
        findViewById(R.id.editTravelMode).setVisibility(View.VISIBLE);

        findViewById(R.id.travelModeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] typesList = new String[TravelMode.values().length];
                for (TravelMode travelMode : TravelMode.values()) {
                    typesList[travelMode.getId()] = travelMode.name();
                }

                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.chooseTravelMode)
                        .setSingleChoiceItems(typesList, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TravelMode.byId(which) == TravelMode.none) {
                                    profile.setTravelMode(TravelMode.byId(which));
                                    setHostStatus(true, profile);
                                    findViewById(R.id.hostLayout).setEnabled(false);
                                    findViewById(R.id.switchHost).setEnabled(false);
                                } else {
                                    profile.setTravelMode(TravelMode.byId(which));
                                    findViewById(R.id.hostLayout).setEnabled(true);
                                    findViewById(R.id.switchHost).setEnabled(true);
                                }
                                drawEditTravelMode(profile);
                                drawEditHost(profile);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    private void drawEditHost(final Profile profile) {
        ((Switch) findViewById(R.id.switchHost)).setVisibility(View.VISIBLE);
        ((Switch) findViewById(R.id.switchHost)).setChecked(profile.getHost());
        findViewById(R.id.hostLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getHost()) {
                    setHostStatus(false, profile);
                } else {
                    setHostStatus(true, profile);
                }
            }
        });
        ((Switch) findViewById(R.id.switchHost)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setHostStatus(isChecked, profile);
            }
        });
    }

    private void setTravelModeStatus(Profile profile) {
        ((TextView) findViewById(R.id.travelModeName)).setText(profile.getTravelMode().name());
        ((ImageView) findViewById(R.id.travelModeImage)).setImageResource(profile.getTravelMode().getImage());
    }

    private void setHostStatus(boolean isChecked, Profile profile) {
        if (isChecked) {
            profile.setHost(isChecked);
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.yes);
        } else {
            profile.setHost(isChecked);
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.no);
        }
        ((Switch) findViewById(R.id.switchHost)).setChecked(profile.getHost());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    FirestoreReference.createImageReference(this, data.getData(), (ImageView) findViewById(R.id.profileImage));
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebugMessage.popup(this, "Cancelled");
            }
        }
    }

    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    public void zoomImageFromThumb(final View thumbView, Drawable drawable) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expandedImage);
        expandedImageView.setImageDrawable(drawable);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
