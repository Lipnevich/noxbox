package live.noxbox.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.detailed.CommentAdapter;
import live.noxbox.model.Comment;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.FirestoreReference;
import live.noxbox.tools.Task;

public class ProfilePerformerActivity extends AppCompatActivity {

    public static final int CODE = 1011;
    public static final int SELECT_IMAGE_CERTIFICATE = 1012;
    public static final int SELECT_IMAGE_WORK_SAMPLE = 1013;
    private NoxboxType type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        type = NoxboxType.byId(intent.getIntExtra(ProfileActivity.class.getName(), 0));
        setTitle(type.getName());
        setContentView(R.layout.activity_profile_performer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProfileStorage.listenProfile(ProfilePerformerActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });

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
                profile.getPortfolio().remove(type.name());
                finish();
            }
        });
        if (profile.getPortfolio().get(type.name()) != null) {
            drawComments(profile);
            drawCertificate(profile);
            drawWorkSample(profile);
        }

    }

    private void drawComments(final Profile profile) {
        if (profile.getPortfolio().get(type.name()).getRating().getComments().get("0") != null) {
            findViewById(R.id.commentsCleanText).setVisibility(View.GONE);
            findViewById(R.id.commentsListLayout).setVisibility(View.VISIBLE);

            List<Comment> comments = new ArrayList<>();
            Iterator iterator = profile.getPortfolio().get(type.name()).getRating().getComments().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                comments.add((Comment) pair.getValue());
            }

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.commentsList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new CommentAdapter(comments));

        } else {
            findViewById(R.id.commentsCleanText).setVisibility(View.VISIBLE);
            findViewById(R.id.commentsListLayout).setVisibility(View.GONE);
        }
    }

    private RecyclerView certificateList;

    private void drawCertificate(final Profile profile) {
        List<String> certificateUrlList = profile.getPortfolio().get(type.name()).getCertificates();
        if (certificateUrlList.size() >= 1
                && certificateUrlList.get(0) != null) {

            certificateList = (RecyclerView) findViewById(R.id.certificatesList);
            certificateList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            certificateList.setAdapter(new ImageListAdapter(certificateUrlList, this));

            findViewById(R.id.certificateLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            setOnItemCertificateClickListener(certificateList, certificateUrlList);
        }

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

    private RecyclerView workSampleList;

    private void drawWorkSample(final Profile profile) {
        List<String> workSampleUrlList = profile.getPortfolio().get(type.name()).getWorkSamples();
        if (workSampleUrlList.size() >= 1
                && workSampleUrlList.get(0) != null) {

            workSampleList = (RecyclerView) findViewById(R.id.workSampleList);
            workSampleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            workSampleList.setAdapter(new ImageListAdapter(workSampleUrlList, this));

            findViewById(R.id.workSampleLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            setOnItemCertificateClickListener(workSampleList, workSampleUrlList);
        }
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


    private <T extends String> void setOnItemCertificateClickListener(RecyclerView recyclerView, final List<T> imageUrlList) {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("photos", (Serializable) imageUrlList);
                bundle.putInt("position", position);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment slideShowFragment = SlideshowDialogFragment.newInstance();
                slideShowFragment.setArguments(bundle);
                slideShowFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE_CERTIFICATE) {
                if (data != null) {
                    FirestoreReference.createImageReference(this, data.getData(), (ImageView) findViewById(R.id.profileImage), "certificates");
                }
            }
            if (requestCode == SELECT_IMAGE_WORK_SAMPLE) {
                if (data != null) {
                    FirestoreReference.createImageReference(this, data.getData(), (ImageView) findViewById(R.id.profileImage), "workSamples");
                }
            }
        }
    }
}
