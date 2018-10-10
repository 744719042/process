package com.example.process;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Application extends android.app.Application {
    private static final String TAG = "Application";
    private static final String ARG_UNREGISTER_CLASS_NAME = "arg_unregister_class_name";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            hookStartActivity(this);
            hookPackageManager(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        hookLaunchActivity();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(new ScreenBroadcastReceiver(), intentFilter);

    }

    private void hookPackageManager(Application application) {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field getPackageManager = activityThreadClass.getDeclaredField("sPackageManager");
            getPackageManager.setAccessible(true);
            final Object packageManager = getPackageManager.get(null);
            Class<?> ipackage = Class.forName("android.content.pm.IPackageManager");
            Object newPackagManager = Proxy.newProxyInstance(getClassLoader(), new Class[]{ipackage}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("getActivityInfo")) {
                        return new ActivityInfo();
                    }
                    return method.invoke(packageManager, args);
                }
            });
            getPackageManager.set(null, newPackagManager);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    private void hookLaunchActivity() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field field = activityThreadClass.getDeclaredField("mH");
            field.setAccessible(true);
            Handler handler = (Handler) field.get(activityThread);
            hookHandler(handler);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void hookHandler(Handler handler) throws IllegalAccessException,
            IllegalArgumentException, NoSuchFieldException {
        Field field = Handler.class.getDeclaredField("mCallback");
        field.setAccessible(true);
        field.set(handler, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 100) {
                    try {
                        Class<?> recordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                        Field intentField = recordClass.getDeclaredField("intent");
                        intentField.setAccessible(true);
                        Intent intent = (Intent) intentField.get(msg.obj);
                        String className = intent.getStringExtra(ARG_UNREGISTER_CLASS_NAME);
                        if (!TextUtils.isEmpty(className)) {
                            intent = new Intent();
                            intent.setComponent(new ComponentName(getPackageName(), className));
                            intentField.set(msg.obj, intent);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    private void hookStartActivity(Context context) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> amn = Class.forName("android.app.ActivityManagerNative");
        Field gDefaultField = amn.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true);
        Object gDefault = gDefaultField.get(null);
        Class<?> singletonClass = Class.forName("android.util.Singleton");
        Field instanceField = singletonClass.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        Object instance = instanceField.get(gDefault);
        Class<?> inter = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(getClassLoader(), new Class[]{ inter }, new StartActivityInvocationHandler(context, instance));
        instanceField.set(gDefault, proxy);
    }

    private static class StartActivityInvocationHandler implements InvocationHandler {
        private Object object;
        private Context context;

        public StartActivityInvocationHandler(Context context, Object object) {
            this.object = object;
            this.context = context;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("startActivity")) {
                Intent intent = (Intent) args[2];
                if (intent.getComponent() != null) {
                    String className = intent.getComponent().getClassName();
                    if (className.contains("Unregistered")) {
                        intent = new Intent(context, ProxyActivity.class);
                        intent.putExtra(ARG_UNREGISTER_CLASS_NAME, className);
                        args[2] = intent;
                    }
                }
            }
            return method.invoke(object, args);
        }
    }

//    private void hookStartActivity() {
//        try {
//            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
//            Log.e(TAG, activityThreadClass.toString());
//            Method method = activityThreadClass.getDeclaredMethod("currentActivityThread");
//            method.setAccessible(true);
//            Object activityThread = method.invoke(null);
//            Log.e(TAG, activityThread.toString());
//            Field instrumentation = activityThreadClass.getDeclaredField("mInstrumentation");
//            instrumentation.setAccessible(true);
//            Instrumentation old = (Instrumentation) instrumentation.get(activityThread);
//            Log.e(TAG, old.toString());
//            Instrumentation newInstrumentation = new MyInstrumentation(old);
//            instrumentation.set(activityThread, newInstrumentation);
//            Log.e(TAG, "init instrumentation success");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static class MyInstrumentation extends Instrumentation {
//        private Object old;
//
//        public MyInstrumentation(Object old) {
//            this.old = old;
//        }
//
//        public ActivityResult execStartActivity(
//                Context who, IBinder contextThread, IBinder token, Activity target,
//                Intent intent, int requestCode, Bundle options) {
//                Log.e(TAG, "execStartActivity = " + intent.getComponent());
//                if (intent.getComponent() != null) {
//                    String className = intent.getComponent().getClassName();
//                    if (className.contains("Unregistered")) {
//                        intent = new Intent(who, ProxyActivity.class);
//                        intent.putExtra(ARG_UNREGISTER_CLASS_NAME, className);
//                    }
//                }
//
//                Log.e(TAG, "execStartActivity !!!!!!!!!!!!!!!!!!!!");
//
//            try {
//                Method method = Instrumentation.class.getDeclaredMethod("execStartActivity",
//                        Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class);
//                return (ActivityResult) method.invoke(old, who, contextThread, token, target, intent, requestCode, options);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//
//    }
}
