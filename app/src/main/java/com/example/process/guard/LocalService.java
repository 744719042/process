package com.example.process.guard;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.process.IProcessGuard;


public class LocalService extends Service {
    private static final String TAG = "LocalService";

    private static final String ACTION_GUARD_REMOTE_SERVICE = "action_guard_remote_service";
    private IProcessGuard mGuard;

    private ServiceConnection serviceConnection;

    public LocalService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "bind to remote service success");
                mGuard = IProcessGuard.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "remote service died, rebind");
                Intent serviceIntent = new Intent();
                serviceIntent.setAction(ACTION_GUARD_REMOTE_SERVICE);
                serviceIntent.setPackage(getPackageName());
                startService(serviceIntent);
                bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(ACTION_GUARD_REMOTE_SERVICE);
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IProcessGuard.Stub() {
            @Override
            public void restart() throws RemoteException {

            }
        };
    }
}
