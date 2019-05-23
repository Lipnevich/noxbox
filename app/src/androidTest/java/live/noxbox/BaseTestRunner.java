package live.noxbox;

import android.util.Log;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Created by Vladislaw Kravchenok on 23.05.2019.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public abstract class BaseTestRunner {
    @Rule
    public ActivityTestRule<MapActivity> rule = new ActivityTestRule<MapActivity>(MapActivity.class);

    protected void sleep(Long millis) {
        try {
            Thread.sleep(millis);//nightmare way
        } catch (Exception e) {
            Log.e("BaseTestRunner: ", e.toString());
        }
    }
}
