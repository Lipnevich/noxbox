package live.noxbox.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.detailed.CommentAdapter;
import live.noxbox.model.Comment;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.Task;

public class ProfilePerformerActivity extends AppCompatActivity {

    public static final int CODE = 1011;
    private NoxboxType type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_performer);

        Intent intent = getIntent();
        type = NoxboxType.byId(intent.getIntExtra(ProfileActivity.class.getName(), 0));


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
            int i = 0;
            while (true) {
                if (profile.getPortfolio().get(type.name()).getRating().getComments().get(String.valueOf(i)) == null) {
                    break;
                }
                comments.add(profile.getPortfolio().get(type.name()).getRating().getComments().get(String.valueOf(i)));
                i++;
            }

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.commentsList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new CommentAdapter(comments));

        } else {
            findViewById(R.id.commentsCleanText).setVisibility(View.VISIBLE);
            findViewById(R.id.commentsListLayout).setVisibility(View.GONE);
        }
    }

    private void drawCertificate(final Profile profile) {
        List<String> certificateUrlList = profile.getPortfolio().get(type.name()).getCertificates();
        if (certificateUrlList.size() >= 1
                && certificateUrlList.get(0) != null) {

            RecyclerView certificateList = (RecyclerView) findViewById(R.id.certificatesList);
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
                //TODO (vl) add new certificate from
            }
        });
    }

    private void drawWorkSample(final Profile profile) {
        List<String> workSampleUrlList = profile.getPortfolio().get(type.name()).getWorkSamples();
        if (workSampleUrlList.size() >= 1
                && workSampleUrlList.get(0) != null) {

            RecyclerView workSampleList = (RecyclerView) findViewById(R.id.workSampleList);
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
                //TODO (vl) add new work sample
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


}
