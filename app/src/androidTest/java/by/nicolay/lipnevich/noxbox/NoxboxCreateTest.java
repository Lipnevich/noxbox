package by.nicolay.lipnevich.noxbox;

import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.database.AppCache;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static by.nicolay.lipnevich.noxbox.Utils.getItemByText;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Vladislaw Kravchenok on 21.05.2019.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class NoxboxCreateTest {
    //TODO (vl) try to find the way without Thread.sleep()
    @Rule
    public ActivityTestRule<MapActivity> rule = new ActivityTestRule<MapActivity>(MapActivity.class);

    @Before
    public void setUp() throws Exception {
        onView(withId(R.id.checkbox)).perform(click());
        onView(withId(R.id.googleAuth)).perform(ViewActions.click());
        Thread.sleep(8000);//bad way
        UiObject account = getItemByText("testnoxbox2018@Gmail.com");
        if (account.exists()) {
            account.clickAndWaitForNewWindow();
        }
        Thread.sleep(10000);//bad way
    }

    @Test
    public void checkProfile_hasReady() {
        Assert.assertThat(AppCache.isProfileReady(), equalTo(true));
        closeMapActivity();
    }

    @Test
    public void checkContractButton_hasVisible() {
        onView(withId(R.id.customFloatingView)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(view.getVisibility(), View.VISIBLE);
            }
        });
        closeMapActivity();
    }

    @Test
    public void clickContractButton_contractActivityHasShown() throws Exception{
        onView(withId(R.id.customFloatingView)).perform(ViewActions.click());
        Thread.sleep(3000);//bad way
        Assert.assertThat(Utils.getActivityInstance().getClass().getName(),equalTo(ContractActivity.class.getName()));
        closeContractActivity();
        Thread.sleep(1000);//bad way
        closeMapActivity();
    }

    private void closeMapActivity() {
        onView(withId(R.id.menu)).perform(ViewActions.click());
        onView(withText("Logout")).perform(ViewActions.click());
    }
    private void closeContractActivity() {
        onView(withId(R.id.homeButton)).perform(ViewActions.click());
    }
}
