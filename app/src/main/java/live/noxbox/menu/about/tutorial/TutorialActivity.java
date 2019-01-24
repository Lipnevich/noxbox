package live.noxbox.menu.about.tutorial;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.tools.GyroscopeObserver;
import live.noxbox.tools.PanoramaImageView;

public class TutorialActivity extends BaseActivity {
    private GyroscopeObserver gyroscopeObserver;
    private ImageView intro;
    private Button skip;
    private Button next;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tutorial);

        intro = findViewById(R.id.intro);
        intro.setImageResource(R.drawable.tutorial_intro_one);
        skip = findViewById(R.id.skip);
        next = findViewById(R.id.next);

        final PanoramaImageView panoramaImageView = findViewById(R.id.tutorialBackground);
        gyroscopeObserver = new GyroscopeObserver();
        // Set the maximum radian the device should rotate to show image's bounds.
        // It should be set between 0 and π/2.
        // The default value is π/9.
        gyroscopeObserver.setMaxRotateRadian(Math.PI / 4);
        gyroscopeObserver.addPanoramaImageView(panoramaImageView);

        ((ViewPager) findViewById(R.id.viewpager)).setAdapter(new TutorialAdapter(getSupportFragmentManager()));
        ((ViewPager) findViewById(R.id.viewpager)).addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        intro.setImageResource(R.drawable.tutorial_intro_one);
                        next.setText("NEXT");
                        break;
                    case 1:
                        intro.setImageResource(R.drawable.tutorial_intro_two);
                        next.setText("NEXT");
                        break;
                    case 2:
                        intro.setImageResource(R.drawable.tutorial_intro_three);
                        next.setText("NEXT");
                        break;
                    case 3:
                        intro.setImageResource(R.drawable.tutorial_intro_four);
                        next.setText("GOT IT");
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
