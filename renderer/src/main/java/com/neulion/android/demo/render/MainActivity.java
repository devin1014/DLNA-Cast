package com.neulion.android.demo.render;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.neulion.android.upnpcast.renderer.KeepLiveJobService;
import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-25
 * Time: 16:15
 */
public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        startService(new Intent(this, NLUpnpRendererService.class));

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP)
        {
            KeepLiveJobService.startJobScheduler(this);
        }
    }
}
