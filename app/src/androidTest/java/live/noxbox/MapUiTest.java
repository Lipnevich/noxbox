package live.noxbox;

import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static live.noxbox.Utils.login;
import static live.noxbox.Utils.logout;

/**
 * Created by Vladislaw Kravchenok on 22.05.2019.
 */
public class MapUiTest extends BaseTestRunner {


    @Before
    public void signIn() throws Exception {
        login();
    }

    @Test
    public void checkContractButton_hasVisible() {
        onView(withId(R.id.customFloatingView)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                Assert.assertEquals(view.getVisibility(), View.VISIBLE);
            }
        });

        logout();
    }



}
