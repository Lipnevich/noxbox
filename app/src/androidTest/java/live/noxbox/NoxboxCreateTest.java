package live.noxbox;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import live.noxbox.model.NoxboxState;

import static live.noxbox.Utils.login;
import static live.noxbox.Utils.registerIdlingResource;
import static live.noxbox.database.AppCache.profile;

/**
 * Created by Vladislaw Kravchenok on 21.05.2019.
 */
public class NoxboxCreateTest extends NoxboxTest {

    @Before
    public void signIn() throws Exception {
        login();
        idlingResource = ProfileListenerIdlingResource.getInstance();
        registerIdlingResource(idlingResource);
    }

    @Test
    public void createNoxbox_hasCreated() throws Exception {
        openContract();
        fillNoxbox();
        publishNoxbox();
        Assert.assertEquals(NoxboxState.getState(profile().getCurrent(), profile()), NoxboxState.created);
        removeNoxbox();
        Utils.logout();
    }

    @After
    public void unregister() {
        Utils.unregisterIdlingResource(idlingResource);
    }

}
