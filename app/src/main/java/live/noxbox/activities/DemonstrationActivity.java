package live.noxbox.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.widget.LinearLayout;

import live.noxbox.R;
import live.noxbox.debug.HackerActivity;
import live.noxbox.ui.ArrowView;

import static live.noxbox.Constants.FIRST_DEMONSTRATION_KEY;
import static live.noxbox.ui.ArrowView.FROM_OR_TO_BOTTOM_CENTER;
import static live.noxbox.ui.ArrowView.FROM_OR_TO_END_CENTER;
import static live.noxbox.ui.ArrowView.FROM_OR_TO_START_CENTER;
import static live.noxbox.ui.ArrowView.FROM_OR_TO_TOP_CENTER;

/**
 * Created by Vladislaw Kravchenok on 08.04.2019.
 */
public class DemonstrationActivity extends HackerActivity {

    private SharedPreferences demonstrationPreference;
    private LinearLayout container;


    protected boolean isFirstRunDemonstration() {
        if (demonstrationPreference == null) {
            demonstrationPreference = getApplicationContext().getSharedPreferences(FIRST_DEMONSTRATION_KEY, MODE_PRIVATE);
        }

        if (demonstrationPreference.getBoolean(FIRST_DEMONSTRATION_KEY, true)) {
            demonstrationPreference.edit().putBoolean(FIRST_DEMONSTRATION_KEY, false).apply();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        showDemonstration();
        super.onResume();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showDemonstration();
    }

    static boolean isShown = false;

    protected void showDemonstration() {

        if (isFirstRunDemonstration()) {

            container = findViewById(R.id.container);
            View child = getLayoutInflater().inflate(R.layout.activity_demonstration, null);
            container.addView(child);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.removeAllViews();
                    container = null;
                }
            });

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ArrowView arrowView2 = container.findViewById(R.id.arrowView2);
                arrowView2.setWillNotDraw(false);
                arrowView2.setStartView(container.findViewById(R.id.textView2));
                arrowView2.setEndView(container.findViewById(R.id.button));
                arrowView2.invalidate(FROM_OR_TO_START_CENTER, FROM_OR_TO_BOTTOM_CENTER);

                ArrowView arrowView3 = container.findViewById(R.id.arrowView3);
                arrowView3.setWillNotDraw(false);
                arrowView3.setStartView(container.findViewById(R.id.textView3));
                arrowView3.setEndView(container.findViewById(R.id.button2));
                arrowView3.invalidate(FROM_OR_TO_END_CENTER, FROM_OR_TO_BOTTOM_CENTER);

                ArrowView arrowView4 = container.findViewById(R.id.arrowView4);
                arrowView4.setWillNotDraw(false);
                arrowView4.setStartView(container.findViewById(R.id.textView4));
                arrowView4.setEndView(container.findViewById(R.id.button3));
                arrowView4.invalidate(FROM_OR_TO_START_CENTER, FROM_OR_TO_TOP_CENTER);

                ArrowView arrowView5 = container.findViewById(R.id.arrowView5);
                arrowView5.setWillNotDraw(false);
                arrowView5.setStartView(container.findViewById(R.id.textView5));
                arrowView5.setEndView(container.findViewById(R.id.button4));
                arrowView5.invalidate(FROM_OR_TO_END_CENTER, FROM_OR_TO_TOP_CENTER);

            } else {
                ArrowView arrowView2 = container.findViewById(R.id.arrowView2);
                arrowView2.setWillNotDraw(false);
                arrowView2.setStartView(container.findViewById(R.id.textView2));
                arrowView2.setEndView(container.findViewById(R.id.button));
                arrowView2.invalidate(FROM_OR_TO_START_CENTER, FROM_OR_TO_END_CENTER);

                ArrowView arrowView3 = container.findViewById(R.id.arrowView3);
                arrowView3.setWillNotDraw(false);
                arrowView3.setStartView(container.findViewById(R.id.textView3));
                arrowView3.setEndView(container.findViewById(R.id.button2));
                arrowView3.invalidate(FROM_OR_TO_END_CENTER, FROM_OR_TO_START_CENTER);

                ArrowView arrowView4 = container.findViewById(R.id.arrowView4);
                arrowView4.setWillNotDraw(false);
                arrowView4.setStartView(container.findViewById(R.id.textView4));
                arrowView4.setEndView(container.findViewById(R.id.button3));
                arrowView4.invalidate(FROM_OR_TO_START_CENTER, FROM_OR_TO_END_CENTER);

                ArrowView arrowView5 = container.findViewById(R.id.arrowView5);
                arrowView5.setWillNotDraw(false);
                arrowView5.setStartView(container.findViewById(R.id.textView5));
                arrowView5.setEndView(container.findViewById(R.id.button4));
                arrowView5.invalidate(FROM_OR_TO_END_CENTER, FROM_OR_TO_START_CENTER);

            }


        }
    }

}
