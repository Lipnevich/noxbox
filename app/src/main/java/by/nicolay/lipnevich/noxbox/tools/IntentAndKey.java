package by.nicolay.lipnevich.noxbox.tools;

import android.content.Intent;

public class IntentAndKey {

    private Intent intent;
    private int key;

    public Intent getIntent() {
        return intent;
    }

    public IntentAndKey setIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

    public int getKey() {
        return key;
    }

    public IntentAndKey setKey(int key) {
        this.key = key;
        return this;
    }
}
