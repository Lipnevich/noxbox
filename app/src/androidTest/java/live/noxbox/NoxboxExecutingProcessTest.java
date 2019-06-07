package live.noxbox;

import android.content.Context;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import live.noxbox.activities.ConfirmationActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static live.noxbox.Utils.getActivityInstance;
import static live.noxbox.Utils.getItemByText;
import static live.noxbox.Utils.login;
import static live.noxbox.Utils.logout;
import static live.noxbox.Utils.mailParty;
import static live.noxbox.Utils.registerIdlingResource;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.menu.history.HistoryAdapter.historyDemandCache;
import static live.noxbox.menu.history.HistoryAdapter.historySupplyCache;
import static live.noxbox.states.Performing.SERVICE_COMPLETING_BUTTON_ID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * Created by Vladislaw Kravchenok on 23.05.2019.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoxboxExecutingProcessTest extends NoxboxTest {
    private static NoxboxState nextState = NoxboxState.initial;
    private static boolean hasPartyVerified;
    private static String partyBalance;
    private static String noxboxId;
    private Context context;

    @Before
    public void signIn() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        AppCache.hasNoxboxTestProcessing = true;
        switch (nextState) {
            case requesting: {
                login(mailParty);
                break;
            }
            case accepting: {
                login();
                break;
            }
            case moving: {
                if (hasPartyVerified) {
                    login(mailParty);
                }
                break;
            }
            case performing: {
                break;
            }
            default:
                login();
        }
        idlingResource = ProfileListenerIdlingResource.getInstance();
        registerIdlingResource(idlingResource);
    }

    @Test
    public void aPublishNoxbox_hasPublished() {
        openContract();
        fillNoxbox();
        publishNoxbox();
        Assert.assertEquals(NoxboxState.getState(profile().getCurrent(), profile()), NoxboxState.created);

        noxboxId = profile().getCurrent().getId();
        nextState = NoxboxState.requesting;
        Utils.logout();
    }

    @Test
    public void bRequestNoxbox_hasRequested() throws Exception {
        onView(withId(R.id.locationButton)).perform(click());
        sleep(1000L);
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject marker = device.findObject(new UiSelector().descriptionContains(noxboxPosition.getLatitude() + "" + noxboxPosition.getLongitude()));
        marker.click();
        sleep(5000L);
        onView(withId(R.id.joinButton)).perform(click());

        Assert.assertThat(profile().getCurrent().getTimeRequested(), Matchers.allOf(Matchers.not(nullValue()), Matchers.not(0)));
        partyBalance = profile().getWallet().getBalance();
        nextState = NoxboxState.accepting;
        logout();
    }

    @Test
    public void cAcceptNoxbox_hasAccepted() throws UiObjectNotFoundException {
        GeoRealtime.offline(profile().getCurrent());
        sleep(1000L);
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject marker = device.findObject(new UiSelector().descriptionContains(noxboxPosition.getLatitude() + "" + noxboxPosition.getLongitude()));
        marker.click();
        sleep(4000L);
        onView(withId(R.id.acceptButton)).perform(click());
        sleep(5000L);
        Assert.assertThat(NoxboxState.getState(profile().getCurrent(), profile()), is(NoxboxState.moving));
        nextState = NoxboxState.moving;
    }

    @Test
    public void dSendMessageInChat_messageHasExist() {
        onView(ViewMatchers.withId(R.id.chat)).perform(ViewActions.click());
        sleep(4000L);
        onView(ViewMatchers.withId(R.id.type_message)).perform(ViewActions.replaceText(chatMessage));
        onView(ViewMatchers.withId(R.id.send_message)).perform(ViewActions.click());
        sleep(1000L);
        onView(ViewMatchers.withId(R.id.homeButton)).perform(ViewActions.click());
        sleep(3000L);
        Assert.assertThat(profile().getCurrent().getChat().getOwnerMessages().size(), allOf(is(not(0)), is(not(nullValue()))));
    }

    @Test
    public void eVerifyPartyPhoto_partyHasVerified() {
        onView(ViewMatchers.withId(R.id.customFloatingView)).perform(click());
        sleep(4000L);
        boolean isSwiped = false;
        while (!isSwiped) {
            onView(ViewMatchers.withId(R.id.swipeButtonConfirm)).perform(ViewActions.swipeRight());
            sleep(4000L);
            if (!getActivityInstance().getClass().getName().equals(ConfirmationActivity.class.getName())) {
                isSwiped = true;
            }
        }
        Assert.assertThat(profile().getCurrent().getTimeOwnerVerified(), allOf(is(not(0)), is(not(nullValue()))));
        hasPartyVerified = true;
        logout();
    }

    @Test
    public void fVerifyOwnerPhoto_ownerHasVerified() {
        onView(ViewMatchers.withId(R.id.customFloatingView)).perform(click());
        sleep(4000L);

        boolean isSwiped = false;
        while (!isSwiped) {
            onView(ViewMatchers.withId(R.id.swipeButtonConfirm)).perform(ViewActions.swipeRight());
            sleep(4000L);
            if (!getActivityInstance().getClass().getName().equals(ConfirmationActivity.class.getName())) {
                isSwiped = true;
            }
        }


        Assert.assertThat(profile().getCurrent().getTimePartyVerified(), allOf(is(not(0)), is(not(nullValue()))));
        nextState = NoxboxState.performing;
    }

    @Test
    public void gPartyWillFinish_noxboxHasFinished() {
        boolean isSwiped = false;
        while (!isSwiped) {
            onView(withId(SERVICE_COMPLETING_BUTTON_ID)).perform(ViewActions.swipeRight());
            sleep(4000L);
            if (profile().getCurrent().getFinished()) {
                isSwiped = true;
            }
        }
        sleep(30000L);
        Assert.assertTrue(profile().getCurrent().getFinished());
    }

    @Test
    public void hCheckBalance_balanceHasChanged() {
        sleep(10000L);
        Assert.assertThat(profile().getWallet().getBalance(), not(equalTo(partyBalance)));
    }

    @Test
    public void iCheckHistory_finishedNoxboxIsThere() throws UiObjectNotFoundException {
        Espresso.onView(ViewMatchers.withId(R.id.menu)).perform(ViewActions.click());
        getItemByText("History").click();
        sleep(10000L);
        boolean noxboxIsThere = false;
        if (role == MarketRole.supply) {
            for (Noxbox noxbox : historyDemandCache) {
                if (noxbox.getId().equals(noxboxId)) {
                    noxboxIsThere = true;
                }
            }
        } else {
            for (Noxbox noxbox : historySupplyCache) {
                if (noxbox.getId().equals(noxboxId)) {
                    noxboxIsThere = true;
                }
            }
        }

        Assert.assertTrue(noxboxIsThere);
    }

    @Test
    public void jSendCommentForParty_commentHasSent() throws UiObjectNotFoundException {
        Espresso.onView(ViewMatchers.withId(R.id.menu)).perform(ViewActions.click());
        getItemByText("History").click();
        sleep(10000L);
        onView(allOf(withId(R.id.comment), isDisplayed())).perform(ViewActions.replaceText(commentMessage));
        onView(allOf(withId(R.id.send), isDisplayed())).perform(ViewActions.click());
        sleep(4000L);
        Assert.assertThat(profile().getCurrent().getPartyComment(), is(not(nullValue())));
        onView(withId(R.id.homeButton)).perform(click());
        sleep(5000L);
        logout();
    }

    @After
    public void unregister() {
        AppCache.hasNoxboxTestProcessing = false;
        Utils.unregisterIdlingResource(idlingResource);
    }
}
