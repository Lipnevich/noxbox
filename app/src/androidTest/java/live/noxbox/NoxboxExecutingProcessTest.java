package live.noxbox;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import live.noxbox.model.NoxboxState;

import static live.noxbox.Utils.login;
import static live.noxbox.Utils.mailParty;
import static live.noxbox.Utils.registerIdlingResource;
import static live.noxbox.database.AppCache.profile;

/**
 * Created by Vladislaw Kravchenok on 23.05.2019.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoxboxExecutingProcessTest extends NoxboxTest {
    private static NoxboxState nextState = NoxboxState.initial;
    private static boolean isOwnerVerify;

    @Before
    public void signIn() throws Exception {

        switch (nextState) {
            case created: {
                login(mailParty);
                break;
            }
            case accepting: {
                login();
                break;
            }
            case moving: {
                if (!isOwnerVerify) {
                    login();
                } else {
                    login(mailParty);
                }
                break;
            }
            case performing: {
                login();
                break;
            }
            case completed: {
                login();
                break;
            }
            default:
                login();
        }
        idlingResource = ProfileListenerIdlingResource.getInstance();
        registerIdlingResource(idlingResource);
    }

    @Test
    public void publishNoxbox_hasPublished() {
        openContract();
        publishNoxbox();
        Assert.assertEquals(NoxboxState.getState(profile().getCurrent(), profile()), NoxboxState.created);

        nextState = NoxboxState.requesting;
        Utils.logout();
    }

//    @Test
//    public void requestNoxbox_hasRequested() {
//
//        nextState = NoxboxState.accepting;
//    }
//
//    @Test
//    public void acceptNoxbox_hasAccepted() {
//
//        nextState = NoxboxState.moving;
//    }

    @After
    public void unregister() {
        Utils.unregisterIdlingResource(idlingResource);
    }
}
