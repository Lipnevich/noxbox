package live.noxbox;

import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.After;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static live.noxbox.Utils.login;


public class AuthFunctionsTest extends BaseTestRunner{

    @Test
    public void signInWithGoogle_mapAsExpected() throws Exception {
        login();
        onView(ViewMatchers.withId(R.id.mapId)).check(matches(isDisplayed()));
    }

    @After
    public void logout(){
        Utils.logout();
    }
}
