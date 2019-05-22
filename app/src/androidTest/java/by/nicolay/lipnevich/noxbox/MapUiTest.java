package by.nicolay.lipnevich.noxbox;

import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Vladislaw Kravchenok on 22.05.2019.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class MapUiTest {

    @Test
    public void checkContractButton_hasVisible() {
        onView(withId(R.id.customFloatingView)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(view.getVisibility(), View.VISIBLE);
            }
        });
    }
}
