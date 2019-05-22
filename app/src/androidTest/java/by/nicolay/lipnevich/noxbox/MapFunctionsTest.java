package by.nicolay.lipnevich.noxbox;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import live.noxbox.database.AppCache;

/**
 * Created by Vladislaw Kravchenok on 22.05.2019.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class MapFunctionsTest {


    @Test
    public void checkProfile_hasReady() {
        Assert.assertTrue(AppCache.isProfileReady());
    }
}
