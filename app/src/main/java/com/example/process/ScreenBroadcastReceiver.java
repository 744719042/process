package com.example.process;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.process.singleton.PixelActivityManager;

public class ScreenBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_SCREEN_OFF:
                PixelActivityManager.getInstance().start(context);
                break;
            case Intent.ACTION_SCREEN_ON:
                PixelActivityManager.getInstance().finish();
                break;
        }
    }
}
