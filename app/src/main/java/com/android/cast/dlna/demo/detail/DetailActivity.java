package com.android.cast.dlna.demo.detail;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.demo.R;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DLNACastManager.getInstance().bindCastService(this);
    }

    @Override
    protected void onStop() {
        DLNACastManager.getInstance().unbindCastService(this);
        super.onStop();
    }
}
