package com.example.process;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RestartJobService extends JobService {
    public RestartJobService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        JobInfo.Builder builder = new JobInfo.Builder(100,
                new ComponentName(getPackageName(), RestartJobService.class.getName()));
        builder.setPeriodic(500);
        JobInfo jobInfo = builder.build();
        JobScheduler scheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        boolean isAlive = isProtectedServiceAlive();
        if (!isAlive) {
            Intent intent = new Intent(this, ProtectedService.class);
            startService(intent);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private boolean isProtectedServiceAlive() {
        ActivityManager activityManager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo serviceInfo : list) {
            if (serviceInfo.service.getClassName().equals(ProtectedService.class.getName())) {
                return true;
            }
        }

        return false;
    }
}
