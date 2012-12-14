package edu.mit.mobile.android.livingpostcards.app;

import edu.mit.mobile.android.livingpostcards.util.Patches;
import edu.mit.mobile.android.locast.app.LocastApplication;

public class LivingPostcardsApplication extends LocastApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Patches.checkforAndApplyPatches(this);
    }
}
