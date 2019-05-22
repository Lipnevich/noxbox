package by.nicolay.lipnevich.noxbox;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiObject;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;

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
    @Rule
    public ActivityTestRule<MapActivity> rule = new ActivityTestRule<MapActivity>(MapActivity.class);
    private String mail = "testnoxbox2018@gmail.com";
    private Position noxboxPosition = Position.from(new LatLng(79.874390D,23.463329D));
    private ProfileListenerIdlingResource idlingResource;

    @Before
    public void setUp() throws Exception {
        login();
        idlingResource = ProfileListenerIdlingResource.getInstance();
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void clickContractButton_contractActivityHasShown() throws Exception {
        onView(withId(R.id.customFloatingView)).perform(click());
        Thread.sleep(4000);//bad way
        Assert.assertThat(Utils.getActivityInstance().getClass().getName(), equalTo(ContractActivity.class.getName()));
        closeContractActivity();
        Thread.sleep(4000);//bad way
    }

    @Test
    public void createNoxbox_hasCreated() throws Exception {
        onView(withId(R.id.customFloatingView)).perform(ViewActions.click());
        AppCache.profile().getContract().setPosition(noxboxPosition);
        onView(withId(R.id.publish)).perform(ViewActions.click());
        Thread.sleep(5000);//bad way
        Assert.assertEquals(NoxboxState.getState(AppCache.profile().getCurrent(), AppCache.profile()), NoxboxState.created);
        Thread.sleep(5000);//bad way
        onView(ViewMatchers.withId(R.id.customFloatingView)).perform(ViewActions.click());
        Thread.sleep(5000);//bad way
        onView(ViewMatchers.withId(R.id.closeOrRemove)).perform(ViewActions.click());
        Thread.sleep(5000);//bad way
    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource);
        logout();
    }

    private void login() throws Exception {
        onView(withId(R.id.checkbox)).perform(click());
        onView(withId(R.id.googleAuth)).perform(click());
        Thread.sleep(10000);//bad way
        UiObject account = getItemByText(mail);
        if (account.exists()) {
            account.clickAndWaitForNewWindow();
        }
    }

    private void logout() {
        onView(withId(R.id.menu)).perform(click());
        onView(withText("Logout")).perform(click());

    }

    private void closeContractActivity() {
        onView(withId(R.id.homeButton)).perform(click());
    }
}
