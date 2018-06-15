package by.nicolay.lipnevich.noxbox.pages;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.pages.AuthPage;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;


@RunWith(AndroidJUnit4.class)
public class AuthIntegrationTest {

    @Rule
    public ActivityTestRule<AuthPage> rule = new ActivityTestRule<>(AuthPage.class);

    private UiDevice device;

    @Before
    public void before() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testGoogleSignIn() throws Exception {
        Thread.sleep(1000);
        onView(withId(R.id.checkbox)).check(matches(not(isChecked())));
        onView(withId(R.id.googleAuth)).check(matches(not(isEnabled())));
        onView(withId(R.id.checkbox)).perform(click());
        device.findObject(new UiSelector().resourceId("by.nicolay.lipnevich.noxbox:id/googleAuth"))
                .clickAndWaitForNewWindow();

        UiObject account = device.findObject(new UiSelector().textContains("Nicolay.Lipnevich@Gmail.com"));
        account.click();
    }

}
