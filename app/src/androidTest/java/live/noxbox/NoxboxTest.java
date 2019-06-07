package live.noxbox;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.android.gms.maps.model.LatLng;

import live.noxbox.model.MarketRole;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Position;
import live.noxbox.model.TravelMode;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static live.noxbox.database.AppCache.fireProfile;
import static live.noxbox.database.AppCache.profile;

/**
 * Created by Vladislaw Kravchenok on 23.05.2019.
 */

public class NoxboxTest extends BaseTestRunner {
    protected Position noxboxPosition = Position.from(new LatLng(79.874390D, 23.463329D));
    protected NoxboxType noxboxType = NoxboxType.rentRoom;
    protected MarketRole role = MarketRole.supply;
    protected String price = "0.5";
    protected TravelMode travelMode = TravelMode.none;
    protected Boolean host = true;

    protected String chatMessage = "What is nice today? You!";
    protected String commentMessage = "Amazing person! isn't it?";

    protected ProfileListenerIdlingResource idlingResource;

    protected void closeContractActivity() {
        onView(withId(R.id.homeButton)).perform(click());
    }

    protected void openContract() {
        onView(withId(R.id.customFloatingView)).perform(ViewActions.click());
    }

    protected void fillNoxbox() {
        updateNoxboxTypeView();
        updateRoleView();
        updatePriceView();
        updateTravelModeView();
        // updateHost();
        sleep(1000L);
    }

    protected void publishNoxbox() {
        profile().getContract().setPosition(noxboxPosition);
        onView(withId(R.id.publish)).perform(ViewActions.click());
        sleep(5000L);
    }

    protected void removeNoxbox() {
        sleep(5000L);
        openContract();
        sleep(5000L);
        onView(ViewMatchers.withId(R.id.closeOrRemove)).perform(ViewActions.click());
        sleep(5000L);
    }

    protected void updateNoxboxFields() {
        profile().getContract().setType(noxboxType);
        profile().getContract().setRole(role);
        profile().getContract().setPrice(price);
        profile().getContract().getOwner().setTravelMode(travelMode);
        profile().getContract().getOwner().setHost(host);
        fireProfile();
    }

    protected void updateNoxboxTypeView() {
        Espresso.onView(ViewMatchers.withId(R.id.textNoxboxType)).perform(click());
        onView(ViewMatchers.withId(R.id.listOfServices))
                .perform(RecyclerViewActions.actionOnItemAtPosition(noxboxType.getId(),
                        click()));
    }

    protected void updateRoleView() {
        Espresso.onView(ViewMatchers.withId(R.id.textRole)).perform(click());
        Espresso.onView(withText(role.getName())).perform(click());
    }

    protected void updatePriceView() {
        Espresso.onView(withId(R.id.inputPrice)).perform(ViewActions.replaceText(price));
    }

    protected void updateTravelModeView() {
        Espresso.onView(withId(R.id.textTravelMode)).perform(ViewActions.click());
        Espresso.onView(withText(travelMode.getName())).perform(ViewActions.click());
    }

    protected void updateHost() {
        if (host) {
            Espresso.onView(ViewMatchers.withId(R.id.host)).check(ViewAssertions.matches(isNotChecked())).perform(scrollTo(), ViewActions.click());
        } else {
            Espresso.onView(ViewMatchers.withId(R.id.host)).check(ViewAssertions.matches(isChecked())).perform(ViewActions.click());

        }
    }
}
