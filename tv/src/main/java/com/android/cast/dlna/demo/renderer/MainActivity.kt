package com.android.cast.dlna.demo.renderer;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.cast.dlna.core.Utils;
import com.android.cast.dlna.dmr.DLNARendererService;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .request((allGranted, grantedList, deniedList) -> resetWifiInfo());
        DLNARendererService.startService(this);
    }

    private void resetWifiInfo() {
        ((TextView) findViewById(R.id.network_info)).setText(Utils.getWiFiInfoSSID(this));
    }
}