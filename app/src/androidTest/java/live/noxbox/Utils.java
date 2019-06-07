package live.noxbox;

import android.app.Activity;
import android.util.Log;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import java.util.Collection;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * Created by Vladislaw Kravchenok on 21.05.2019.
 */
public class Utils {
    private static Activity currentActivity;

    public static Activity getActivityInstance() {
        currentActivity = null;

        getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> resumedActivities =
                    ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            for (Activity activity : resumedActivities) {
                Log.d("Your current activity: ", activity.getClass().getName());
                currentActivity = activity;
                break;
            }
        });

        return currentActivity;
    }

    public static UiObject getItemByText(String text) {
        return getDevice().findObject(new UiSelector().textContains(text));
    }

    public static UiObject getItemById(String viewId) {
        return getDevice().findObject(new UiSelector().resourceId(viewId));
    }

    public static UiObject getItemByDescription(String description) {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).findObject(new UiSelector().descriptionContains(description));
    }

    public static UiDevice getDevice() {
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    public static String mail = "testnoxbox2018@gmail.com";
    public static String mailParty = "noxboxtestParty@gmail.com";

    public static void login() throws Exception {
        login(null);
    }

    public static void login(String objectText) throws Exception {
        onView(withId(R.id.checkbox)).perform(click());
        onView(withId(R.id.googleAuth)).perform(click());
        Thread.sleep(12000);
        if (objectText == null) {
            getItemByText(mail).click();
        } else {
            getItemByText(objectText).click();
        }
        Thread.sleep(12000);
    }

    public static void logout() {
        onView(withId(R.id.menu)).perform(click());
        try {
            getItemByText("Logout").click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

    }


    public static void registerIdlingResource(IdlingResource idlingResource) {
        IdlingRegistry.getInstance().register(idlingResource);
    }

    public static void unregisterIdlingResource(IdlingResource idlingResource) {
        IdlingRegistry.getInstance().unregister(idlingResource);
    }
}
