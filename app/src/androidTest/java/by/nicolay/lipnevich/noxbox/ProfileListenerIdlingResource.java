package by.nicolay.lipnevich.noxbox;

import androidx.test.espresso.IdlingResource;

import live.noxbox.database.AppCache;

/**
 * Created by Vladislaw Kravchenok on 22.05.2019.
 */
public class ProfileListenerIdlingResource implements IdlingResource {
    private ResourceCallback resourceCallback;
    public static ProfileListenerIdlingResource getInstance(){
        return new ProfileListenerIdlingResource();
    }

    @Override
    public String getName() {
        return ProfileListenerIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean isReady = AppCache.isProfileReady();
        if (isReady && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
        return isReady;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

}
