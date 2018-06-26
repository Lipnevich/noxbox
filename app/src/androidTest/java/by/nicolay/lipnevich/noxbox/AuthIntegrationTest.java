package by.nicolay.lipnevich.noxbox;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import by.nicolay.lipnevich.noxbox.pages.AuthPage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.not;


@RunWith(AndroidJUnit4.class)
public class AuthIntegrationTest {

    @Rule
    public ActivityTestRule<AuthPage> rule = new ActivityTestRule<>(AuthPage.class);

    @Test
    public void testGoogleSignIn() throws Exception {
        Thread.sleep(1000);
        onView(withId(R.id.checkbox)).check(matches(not(isChecked())));
        onView(withId(R.id.googleAuth)).check(matches(not(isEnabled())));
        onView(withId(R.id.checkbox)).perform(click());
        getItemById("googleAuth").clickAndWaitForNewWindow();

        UiObject account = getItemByText("Nicolay.Lipnevich@Gmail.com");
        if(account.exists()) {
            account.clickAndWaitForNewWindow();
        }

        Thread.sleep(5000);
        getItemById("menu").click();

        onView(withText("Logout")).perform(click());
    }

    private UiObject getItemByText(String text) {
        return getDevice().findObject(new UiSelector().textContains(text));
    }

    private UiObject getItemById(String id) {
        return getDevice().findObject(new UiSelector().resourceId(rule.getActivity().getPackageName()
                + ":id/" + id));
    }

    private UiDevice getDevice() {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

}
