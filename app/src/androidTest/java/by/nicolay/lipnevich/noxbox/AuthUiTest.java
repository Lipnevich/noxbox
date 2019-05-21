package by.nicolay.lipnevich.noxbox;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.MapActivity;
import live.noxbox.R;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Vladislaw Kravchenok on 21.05.2019.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class AuthUiTest {
    @Rule
    public ActivityTestRule<MapActivity> authActivityActivityTestRule = new ActivityTestRule<MapActivity>(MapActivity.class);
    private int visible;
    private int invisible;

    @Before
    public void setUp(){
        visible = View.VISIBLE;
        invisible = View.INVISIBLE;
    }

    @Test
    public void checkAgreement_hasVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.agreementView)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertThat(((TextView)view).getVisibility(), equalTo(visible));
            }
        });
    }

    @Test
    public void checkCheckbox_hasVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.checkbox)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertThat(((CheckBox)view).getVisibility(), equalTo(visible));
            }
        });
    }

    @Test
    public void checkGoogleAuthButton_hasInvisible() {
        Espresso.onView(ViewMatchers.withId(R.id.googleAuth)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(((CardView) view).getVisibility(), invisible);
            }
        });
    }

    @Test
    public void checkPhoneAuthButton_hasInvisible() {
        Espresso.onView(ViewMatchers.withId(R.id.phoneAuth)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(((CardView) view).getVisibility(), invisible);
            }
        });
    }

    @Test
    public void clickCheckbox_googleAuthButtonHasVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.checkbox)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.googleAuth)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(((CardView) view).getVisibility(), visible);
            }
        });
    }

    @Test
    public void clickCheckbox_phoneAuthButtonHasVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.checkbox)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.phoneAuth)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(((CardView) view).getVisibility(), visible);
            }
        });
    }
}
