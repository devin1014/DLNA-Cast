package com.neulion.android.demo.render;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

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

        findViewById(R.id.btn_send_wifi_action).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //                sendBroadcast(new Intent("android.net.conn.CONNECTIVITY_CHANGE"));
                //
                //                sendBroadcast(new Intent("android.net.wifi.WIFI_STATE_CHANGED"));
                //
                //                sendBroadcast(new Intent("android.net.wifi.STATE_CHANGE"));
            }
        });
    }
}
