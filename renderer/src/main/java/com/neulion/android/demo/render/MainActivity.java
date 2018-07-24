package com.neulion.android.demo.render;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.TwoStatePreference;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends PreferenceActivity
{
    private static String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler = new Handler();

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "created");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        TwoStatePreference runningPref = findPref("running_switch");

        updateRunningState();

        runningPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                if ((Boolean) newValue)
                {
                    startServer();
                }
                else
                {
                    stopServer();
                }
                return true;
            }
        });

        // Preference selectPref = findPref("media_select");
        // selectPref.setOnPreferenceClickListener(new
        // OnPreferenceClickListener() {
        // @Override
        // public boolean onPreferenceClick(Preference preference) {
        // if (!MediaRendererService.isRunning()) {
        // startActivity(new Intent(MediaPreferenceActivity.this,
        // MainActivity.class));
        // }
        //
        // return false;
        // }
        // });

        EditTextPreference deName_pref = findPref("deName");
        deName_pref.setSummary(MediaSettings.getDeviceName(this));
        deName_pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                String newNameString = (String) newValue;
                if (preference.getSummary().equals(newNameString))
                {
                    return false;
                }
                preference.setSummary(newNameString);
                stopServer();
                return true;
            }
        });

        Preference help = findPref("help");
        help.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Log.v(TAG, "On preference help clicked");
                Context context = MainActivity.this;
                AlertDialog ad = new AlertDialog.Builder(context).setTitle(R.string.help_dlg_title).setMessage(R.string.help_dlg_message).setPositiveButton(R.string.ok, null).create();
                ad.show();
                Linkify.addLinks((TextView) ad.findViewById(android.R.id.message), Linkify.ALL);
                return true;
            }
        });

        Preference about = findPref("about");
        about.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                AlertDialog ad = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.about_dlg_title).setMessage(R.string.about_dlg_message)
                        .setPositiveButton(getText(R.string.ok), null).create();
                ad.show();
                Linkify.addLinks((TextView) ad.findViewById(android.R.id.message), Linkify.ALL);
                return true;
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        updateRunningState();

        Log.v(TAG, "onResume: Registering the server actions");

        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaRendererService.ACTION_STARTED);
        filter.addAction(MediaRendererService.ACTION_STOPPED);
        filter.addAction(MediaRendererService.ACTION_FAILEDTOSTART);
        registerReceiver(mFsActionsReceiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.v(TAG, "onPause: Unregistering the Server actions");
        unregisterReceiver(mFsActionsReceiver);

    }

    private void startServer()
    {
        sendBroadcast(new Intent(MediaRendererService.ACTION_START_RENDER));
    }

    private void stopServer()
    {
        sendBroadcast(new Intent(MediaRendererService.ACTION_STOP_RENDER));
    }

    private void updateRunningState()
    {
        TwoStatePreference runningPref = findPref("running_switch");
        Preference selectPref = findPref("media_select");
        if (MediaRendererService.isRunning())
        {
            selectPref.setEnabled(false);
            runningPref.setSummary(R.string.running_summary_started);
            selectPref.setSummary(R.string.running_select_summary);
        }
        else
        {
            runningPref.setChecked(false);
            runningPref.setSummary(R.string.running_summary_stopped);
            selectPref.setSummary(R.string.select_summary);
            selectPref.setEnabled(true);
        }
    }

    BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.v(TAG, "action received: " + intent.getAction());
            // action will be ACTION_STARTED or ACTION_STOPPED
            updateRunningState();
            // or it might be ACTION_FAILEDTOSTART
            final TwoStatePreference runningPref = findPref("running_switch");
            if (intent.getAction().equals(MediaRendererService.ACTION_FAILEDTOSTART))
            {
                runningPref.setChecked(false);
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        runningPref.setSummary(R.string.running_summary_failed);
                    }
                }, 100);
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        runningPref.setSummary(R.string.running_summary_stopped);
                    }
                }, 5000);
            }
        }
    };

    @SuppressWarnings({"unchecked", "deprecation"})
    protected <T extends Preference> T findPref(CharSequence key)
    {
        return (T) this.findPreference(key);
    }

}
