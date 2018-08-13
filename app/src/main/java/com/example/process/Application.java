package com.example.process;

import android.content.Intent;
import android.content.IntentFilter;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(new ScreenBroadcastReceiver(), intentFilter);
    }
}
