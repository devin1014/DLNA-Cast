package com.android.cast.dlna.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.demo.detail.DetailActivity;
import com.android.cast.dlna.demo.light.LightActivity;
import com.permissionx.guolindev.PermissionX;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private DeviceAdapter mDeviceListAdapter;
    private Fragment mInformationFragment;
    private Fragment mQueryFragment;
    private Fragment mControlFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);
        }
        setContentView(R.layout.activity_main);
        initComponent();
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .request((allGranted, grantedList, deniedList) -> resetToolbar());
    }

    private void initComponent() {
        setSupportActionBar(findViewById(R.id.toolbar));

        mInformationFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_information);
        mQueryFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_query);
        mControlFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_control);

        RadioGroup typeGroup = findViewById(R.id.cast_type_group);
        typeGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        typeGroup.check(R.id.cast_type_info);

        RecyclerView recyclerView = findViewById(R.id.cast_device_list);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mDeviceListAdapter = new DeviceAdapter(this, (castDevice, selected) -> {
            mDeviceListAdapter.setSelectedDevice(selected ? castDevice : null);
            ((IDisplayDevice) mInformationFragment).setCastDevice(selected ? castDevice : null);
            ((IDisplayDevice) mControlFragment).setCastDevice(selected ? castDevice : null);
            ((IDisplayDevice) mQueryFragment).setCastDevice(selected ? castDevice : null);
        }));
        DLNACastManager.getInstance().registerDeviceListener(mDeviceListAdapter);
    }

    private void resetToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("DLNA Cast");
            getSupportActionBar().setSubtitle(NetworkUtils.getWiFiSSID(this));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        resetToolbar();
        DLNACastManager.getInstance().bindCastService(this);
    }

    @Override
    protected void onStop() {
        DLNACastManager.getInstance().unbindCastService(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DLNACastManager.getInstance().unregisterListener(mDeviceListAdapter);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search_start) {
            Toast.makeText(this, "开始搜索", Toast.LENGTH_SHORT).show();
            DLNACastManager.getInstance().search(DLNACastManager.DEVICE_TYPE_DMR, 60);
        } else if (item.getItemId() == R.id.menu_light) {
            startActivity(new Intent(this, LightActivity.class));
        } else if (item.getItemId() == R.id.menu_link_detail) {
            startActivity(new Intent(this, DetailActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private final RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.cast_type_info) {
                getSupportFragmentManager().beginTransaction()
                        .show(mInformationFragment)
                        .hide(mControlFragment)
                        .hide(mQueryFragment)
                        .commit();
            } else if (checkedId == R.id.cast_type_query) {
                getSupportFragmentManager().beginTransaction()
                        .show(mQueryFragment)
                        .hide(mInformationFragment)
                        .hide(mControlFragment)
                        .commit();
            } else if (checkedId == R.id.cast_type_ctrl) {
                getSupportFragmentManager().beginTransaction()
                        .show(mControlFragment)
                        .hide(mInformationFragment)
                        .hide(mQueryFragment)
                        .commit();
            }
        }
    };

}
