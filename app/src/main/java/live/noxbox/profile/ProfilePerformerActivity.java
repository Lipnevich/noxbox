package live.noxbox.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import live.noxbox.BaseActivity;
import live.noxbox.R;
import live.noxbox.model.ImageType;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;
import live.noxbox.model.Profile;
import live.noxbox.state.AppCache;
import live.noxbox.tools.DialogBuilder;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.Task;

import static live.noxbox.tools.ImageManager.deleteFolderByType;

public class ProfilePerformerActivity extends BaseActivity {

    public static final int CODE = 1011;
    public static final int SELECT_IMAGE_CERTIFICATE = 1012;
    public static final int SELECT_IMAGE_WORK_SAMPLE = 1013;
    private NoxboxType type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        type = NoxboxType.byId(intent.getIntExtra(ProfileActivity.class.getName(), 0));
        setContentView(R.layout.activity_profile_performer);
        ((TextView) findViewById(R.id.title)).setText(type.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(ProfilePerformerActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (profile.getPortfolio().get(type.name()) == null) {
                    profile.getPortfolio().put(type.name(), new Portfolio());
                }
                for(ImageType imageType : ImageType.values()) {
                    Map<String, List<String>> images = profile.getPortfolio().get(type.name()).getImages();
                    if(images.get(imageType.name()) == null) {
                        images.put(imageType.name(), new ArrayList<String>());
                    }
                }

                draw(profile);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
    }

    private void draw(final Profile profile) {
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.deleteSection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogBuilder.createSimpleAlertDialog(
                        ProfilePerformerActivity.this,
                        R.string.deleteSection,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                profile.getPortfolio().remove(type.name());
                                deleteFolderByType(type);
                                finish();
                            }
                        });
            }
        });

        drawCertificate(profile);
        drawWorkSample(profile);

    }

    private void drawCertificate(final Profile profile) {

        List<String> certificateUrlList = profile.getPortfolio().get(type.name()).getImages().get(ImageType.certificates.name());

        RecyclerView certificateList = findViewById(R.id.certificatesList);
        certificateList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        certificateList.setAdapter(new ImageListAdapter(certificateUrlList, this, ImageType.certificates, type));

        findViewById(R.id.certificateLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_CERTIFICATE);
            }
        });
    }

    private void drawWorkSample(final Profile profile) {
        List<String> workSampleUrlList = profile.getPortfolio().get(type.name()).getImages().get(ImageType.samples.name());

        RecyclerView workSampleList = findViewById(R.id.workSampleList);
        workSampleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        workSampleList.setAdapter(new ImageListAdapter(workSampleUrlList, this, ImageType.samples, type));


        findViewById(R.id.workSampleLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_WORK_SAMPLE);
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                AppCache.readProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        if (requestCode == SELECT_IMAGE_CERTIFICATE) {
                            ImageManager.uploadImage(ProfilePerformerActivity.this, data.getData(), ImageType.certificates, type, profile.getPortfolio().get(type.name()).getImages().get(ImageType.certificates.name()).size());
                        }
                        if (requestCode == SELECT_IMAGE_WORK_SAMPLE) {
                            ImageManager.uploadImage(ProfilePerformerActivity.this, data.getData(), ImageType.samples, type, profile.getPortfolio().get(type.name()).getImages().get(ImageType.samples.name()).size());
                        }
                    }
                });
            }
        }
    }
}
