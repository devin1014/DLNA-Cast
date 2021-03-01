package com.android.cast.dlna.demo.test;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.cast.dlna.core.Utils;
import com.android.cast.dlna.demo.R;

public class TestMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        String wifiInfoSSID = Utils.getWiFiInfoSSID(this);
        ((TextView) findViewById(R.id.test_info_1)).setText(wifiInfoSSID);
    }
}
