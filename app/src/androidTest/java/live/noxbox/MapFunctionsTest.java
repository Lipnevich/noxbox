package live.noxbox;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import live.noxbox.database.AppCache;

import static live.noxbox.Utils.login;

/**
 * Created by Vladislaw Kravchenok on 22.05.2019.
 */
public class MapFunctionsTest extends BaseTestRunner {

    @Test
    public void signIn_profileHasReady() throws Exception{
        login();
        Assert.assertTrue(AppCache.isProfileReady());
    }

    @After
    public void logout(){
        Utils.logout();
    }

}
