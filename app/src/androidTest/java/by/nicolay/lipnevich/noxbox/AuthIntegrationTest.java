package live.noxbox;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.pages.AuthActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;


@RunWith(AndroidJUnit4.class)
public class AuthIntegrationTest {

    @Rule
    public ActivityTestRule<AuthActivity> rule = new ActivityTestRule<>(AuthActivity.class);

    @Test
    public void testCommonRequested() {
        // open constructor
        // update fields on it
        // post Noxbox
        // become requested
        // open detailed view
        // accept
        // open chat and send message
        // verify
        // become verified
        // complete
    }

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
