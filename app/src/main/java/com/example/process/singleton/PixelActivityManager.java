package com.example.process.singleton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.process.PixelActivity;

import java.lang.ref.WeakReference;

public class PixelActivityManager {
    private WeakReference<Activity> mActivity;

    private PixelActivityManager() {

    }

    private static class PixelActivityManagerHolder {
        private static final PixelActivityManager INSTANCE = new PixelActivityManager();
    }

    public static PixelActivityManager getInstance() {
        return PixelActivityManagerHolder.INSTANCE;
    }

    public void init(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public void finish() {
        if (mActivity != null && mActivity.get() != null) {
            mActivity.get().finish();
        }
    }

    public void start(Context context) {
        Intent intent = new Intent(context, PixelActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
