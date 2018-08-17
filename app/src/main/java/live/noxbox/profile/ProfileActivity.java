package live.noxbox.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Task;

public class ProfileActivity extends AppCompatActivity {

    public static final int CODE = 1006;
    public static final int SELECT_IMAGE = 1007;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProfileStorage.listenProfile(ProfileActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                ((ImageView) findViewById(R.id.travelModeImage)).setImageResource(profile.getTravelMode().getImage());
                draw(profile);
            }
        });
    }

    private void draw(Profile profile) {
        drawPhoto(profile);
        drawName(profile);
        drawTravelMode(profile);

        drawHost(profile);

        drawAboutMe(profile);
        drawCertificates(profile);
        drawWorkSamples(profile);
    }

    private void drawPhoto(Profile profile) {

        findViewById(R.id.toolbar_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
            }
        });

        if (profile.getPhoto() != null) {
            Glide.with(this)
                    .asDrawable()
                    .load(profile.getPhoto())
                    .into((ImageView) findViewById(R.id.profileImage));
            return;
        }
        ((ImageView) findViewById(R.id.profileImage)).setImageResource(R.drawable.unknown_profile);
    }

    private void drawName(Profile profile) {
        getSupportActionBar().setTitle(profile.getName());
    }

    private void drawTravelMode(Profile profile) {
        ((TextView) findViewById(R.id.travelModeName)).setText(profile.getTravelMode().getName());
    }

    private void drawHost(final Profile profile) {
        findViewById(R.id.hostLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getHost()) {
                    profile.setHost(false);
                } else {
                    profile.setHost(true);
                }
                drawHost(profile);
            }
        });

        if (profile.getTravelMode() == TravelMode.none || profile.getHost()) {
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.yes);
        } else {
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.no);
        }

    }

    private void drawAboutMe(Profile profile) {

    }

    private void drawCertificates(Profile profile) {
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

    private void drawWorkSamples(Profile profile) {
        List<String> workSampleList = new ArrayList<>();
        workSampleList.add("http://coolmanicure.com/media/k2/items/cache/stilnyy_manikur_so_strazami_XL.jpg");
        workSampleList.add("http://rosdesign.com/design_materials3/img_materials3/kopf/kopf1.jpg");
        workSampleList.add("http://vmirevolos.ru/wp-content/uploads/2015/12/61.jpg");
        workSampleList.add(" ");
        RecyclerView myList = (RecyclerView) findViewById(R.id.workSampleList);
        myList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myList.setAdapter(new CertificatesAdapter(workSampleList, this));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        ((ImageView) findViewById(R.id.profileImage)).setImageBitmap(bitmap);


                        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("photos").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        //TODO (vl) check photo size <= 1MB
                        UploadTask uploadTask = storageRef.putBytes(baos.toByteArray());
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                        // Got the download URL for 'users/me/profile.png'
                                        ProfileStorage.readProfile(new Task<Profile>() {
                                            @Override
                                            public void execute(final Profile profile) {
                                                profile.setPhoto(uri.toString());
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors

                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                Log.d(ProfileActivity.this.getPackageName(), "Upload is " + progress + "% done");
                            }
                        });


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebugMessage.popup(this, "Cancelled");
            }
        }
    }
}
