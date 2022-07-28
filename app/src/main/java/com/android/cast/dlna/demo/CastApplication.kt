package com.android.cast.dlna.demo

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.android.cast.dlna.dmc.DLNACastManager
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import java.util.logging.Level

class CastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val strategy = PrettyFormatStrategy.newBuilder()
            .tag("DLNA_Cast")
            .methodCount(0)
            .showThreadInfo(true)
            .build()
        DLNACastManager.getInstance().enableLog(strategy, Level.FINEST)
        registerActivityLifecycleCallbacks(LoggingActivityLifecycleCallbacks())
    }
}

private class LoggingActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Logger.d("${getTag(activity)} onActivityCreated")
        (activity as? FragmentActivity)?.supportFragmentManager
            ?.registerFragmentLifecycleCallbacks(LoggingFragmentLifecycleCallbacks(), true)
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.d("${getTag(activity)} onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.d("${getTag(activity)} onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        Logger.d("${getTag(activity)} onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        Logger.d("${getTag(activity)} onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Logger.d("${getTag(activity)} onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Logger.d("${getTag(activity)} onActivityDestroyed")
    }
}

private class LoggingFragmentLifecycleCallbacks : FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        Logger.d("${getTag(f)} onFragmentCreated")
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        Logger.d("${getTag(f)} onFragmentViewCreated")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        Logger.d("${getTag(f)} onFragmentStarted")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        Logger.d("${getTag(f)} onFragmentResumed")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        Logger.d("${getTag(f)} onFragmentPaused")
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        Logger.d("${getTag(f)} onFragmentStopped")
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        Logger.d("${getTag(f)} onFragmentSaveInstanceState")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        Logger.d("${getTag(f)} onFragmentViewDestroyed")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        Logger.d("${getTag(f)} onFragmentDestroyed")
    }
}