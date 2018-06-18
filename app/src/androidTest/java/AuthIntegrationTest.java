import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.pages.AuthPage;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class AuthIntegrationTest {

    @Rule
    public ActivityTestRule<AuthPage> activityTestRule =
            new ActivityTestRule<>(AuthPage.class);

    @Test
    public void testGoogleSignIn() {
        onView(withId(R.id.checkbox)).perform(click());
        onView(withId(R.id.googleAuth)).check(matches(isEnabled()));
        onView(withId(R.id.phoneAuth)).check(matches(isClickable()));
    }

    @Test
    public void checkContainerIsClickable() {
        onView(withId(R.id.checkbox)).perform(click());
        onView(withId(R.id.googleAuth)).check(matches(isClickable()));
        onView(withId(R.id.phoneAuth)).check(matches(isClickable()));
    }
}
