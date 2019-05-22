package by.nicolay.lipnevich.noxbox;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.MapActivity;
import live.noxbox.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static by.nicolay.lipnevich.noxbox.Utils.getActivityInstance;
import static by.nicolay.lipnevich.noxbox.Utils.getItemByText;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4ClassRunner.class)
public class AuthFunctionsTest {

    @Rule
    public ActivityTestRule<MapActivity> rule = new ActivityTestRule<MapActivity>(MapActivity.class);

    @Rule
    public IntentsTestRule<MapActivity> mapActivityIntentsTestRule = new IntentsTestRule<MapActivity>(MapActivity.class);


    // @Test
    //public void testCommonRequested() {
    // open constructor
    // update fields on it
    // post Noxbox
    // become requested
    // open detailed view
    // accept
    // open activity_chat and send message
    // verify
    // become verified
    // complete
    // check balance
    // check activity_history
    // comment in activity_history
    //}
    @Before
    public void setUp(){

    }

    @Test
    public void signInWithGoogle_mapActivityAsExpected() throws Exception {
        onView(withId(R.id.checkbox)).perform(click());
        onView(withId(R.id.googleAuth)).perform(ViewActions.click());
        Thread.sleep(10000);//bad way
        UiObject account = getItemByText("testnoxbox2018@Gmail.com");
        if (account.exists()) {
            account.clickAndWaitForNewWindow();
        }
        Thread.sleep(10000);//bad way
        assertEquals(getActivityInstance().getClass().getName(), MapActivity.class.getName());
        Thread.sleep(10000);//bad way
        closeMapActivity();
    }

    private void closeMapActivity() {
        onView(withId(R.id.menu)).perform(ViewActions.click());
        onView(withText("Logout")).perform(ViewActions.click());
    }


}
