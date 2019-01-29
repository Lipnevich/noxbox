package live.noxbox.menu.about.tutorial;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.debug.GMailSender;
import live.noxbox.tools.GyroscopeObserver;
import live.noxbox.tools.PanoramaImageView;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.TUTORIAL_KEY;
import static live.noxbox.debug.Screenshot.saveToInternalStorage;
import static live.noxbox.debug.Screenshot.takeScreenshot;

public class TutorialActivity extends BaseActivity {
    private GyroscopeObserver gyroscopeObserver;
    private ViewPager viewPager;
    private ImageView intro;
    private Button skip;
    private Button next;

    private String tutorialKey;

    private View.OnClickListener nextOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (viewPager != null) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        }
    };

    private View.OnClickListener gotItOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (viewPager != null) {
                saveToInternalStorage(takeScreenshot(TutorialActivity.this), TutorialActivity.this);
                String mail = "TutorialActivity Screenshot";
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            GMailSender sender = new GMailSender("testnoxbox2018@gmail.com", "noxboxtest");
                            sender.sendMail("deviceInfo",
                                    mail,
                                    "testnoxbox2018@gmail.com",
                                    "support@noxbox.live");
                        } catch (Exception e) {
                            Log.e("GMailSender.class", e.getMessage(), e);
                        }
                        return null;
                    }
                }.execute();

                if (tutorialKey != null && tutorialKey.equals(TUTORIAL_KEY)) {
                    Router.startActivity(TutorialActivity.this, MapActivity.class);
                    finish();
                    return;
                }

                Router.finishActivity(TutorialActivity.this);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long timeStarted = System.currentTimeMillis();
        tutorialKey = getIntent().getStringExtra(TUTORIAL_KEY);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.viewpager);
        intro = findViewById(R.id.intro);
        intro.setImageResource(R.drawable.tutorial_intro_one);
        skip = findViewById(R.id.skip);
        skip.setOnClickListener(gotItOnClickListener);
        next = findViewById(R.id.next);
        next.setOnClickListener(nextOnClickListener);

        final PanoramaImageView panoramaImageView = findViewById(R.id.tutorialBackground);
        gyroscopeObserver = new GyroscopeObserver();
        // Set the maximum radian the device should rotate to show image's bounds.
        // It should be set between 0 and π/2.
        // The default value is π/9.
        gyroscopeObserver.setMaxRotateRadian(Math.PI / 2);
        gyroscopeObserver.addPanoramaImageView(panoramaImageView);

        viewPager.setAdapter(new TutorialAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        intro.setImageResource(R.drawable.tutorial_intro_one);
                        next.setText(R.string.next);
                        next.setOnClickListener(nextOnClickListener);
                        break;
                    case 1:
                        intro.setImageResource(R.drawable.tutorial_intro_two);
                        next.setText(R.string.next);
                        next.setOnClickListener(nextOnClickListener);
                        break;
                    case 2:
                        intro.setImageResource(R.drawable.tutorial_intro_three);
                        next.setText(R.string.next);
                        next.setOnClickListener(nextOnClickListener);
                        break;
                    case 3:
                        intro.setImageResource(R.drawable.tutorial_intro_four);
                        next.setText(R.string.gotIt);
                        next.setOnClickListener(gotItOnClickListener);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        Glide.with(this)
                .asDrawable()
                .load(R.drawable.tutorial_background_with_comets)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        panoramaImageView.setImageDrawable(resource);
                    }
                });

        Log.e("TIMETIMETIMETIMETIME", System.currentTimeMillis() - timeStarted + "");

    }

    @Override
    protected void onResume() {
        super.onResume();

        gyroscopeObserver.register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        gyroscopeObserver.unregister();
    }
}
