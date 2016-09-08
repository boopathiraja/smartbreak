package com.sdl.hellosdlandroid;

import android.app.Application;
import android.content.Intent;

public class SdlApplication extends Application{

    private static final String TAG = SdlApplication.class.getSimpleName();

    private static int CONNECTION_TIMEOUT = 180 * 1000;

    private static SdlApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        LockScreenActivity.registerActivityLifecycle(this);

        mInstance = this;
        Intent proxyIntent = new Intent(this, SdlService.class);
        startService(proxyIntent);
    }

}