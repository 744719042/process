package com.example.process;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class ProtectedService extends Service {
    private static final String TAG = "ProtectedService";
    private ServiceConnection serviceConnection;

    public ProtectedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "bind to local service success");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "local service died, rebind");
                Intent serviceIntent = new Intent(ProtectedService.this, RestartJobService.class);
                startService(serviceIntent);
                bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent serviceIntent = new Intent(ProtectedService.this, RestartJobService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
