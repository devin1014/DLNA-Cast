package com.android.cast.dlna.dmr;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;

import androidx.annotation.RequiresApi;

/**
 *
 */
@TargetApi(VERSION_CODES.LOLLIPOP)
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class KeepLiveJobService extends JobService {

    public static void startJobScheduler(Context context) {
        try {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context, KeepLiveJobService.class));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPeriodic(60 * 1000);
            //builder.setPersisted(true);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        DLNARendererService.startService(getApplicationContext());
        //jobFinished(params, false);
        //return true;
        return false; // start service work has done,so return false! not need to call jobFinished
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
