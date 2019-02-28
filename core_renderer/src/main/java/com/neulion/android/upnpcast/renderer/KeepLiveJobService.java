package com.neulion.android.upnpcast.renderer;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;

import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;

/**
 */

@TargetApi(VERSION_CODES.LOLLIPOP)
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class KeepLiveJobService extends JobService
{
    @SuppressWarnings("ConstantConditions")
    public static void startJobScheduler(Context context)
    {
        try
        {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context, KeepLiveJobService.class));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPeriodic(60 * 1000);
            builder.setPersisted(true);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private ILogger mLogger = new DefaultLoggerImpl(this);

    @Override
    public void onCreate()
    {
        super.onCreate();

        mLogger.i(getClass().getSimpleName() + " onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mLogger.i(getClass().getSimpleName() + " onStartCommand:" + intent);

        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params)
    {
        mLogger.i(getClass().getSimpleName() + " onStartJob");

        NLUpnpRendererService.startService(getApplicationContext());

        //jobFinished(params, false);
        //return true;
        return false; // start service work has done,so return false! not need to call jobFinished
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        mLogger.i(getClass().getSimpleName() + " onStopJob");

        return true;
    }

    @Override
    public void onDestroy()
    {
        mLogger.i(getClass().getSimpleName() + " onDestroy");

        super.onDestroy();
    }
}
