package com.android.cast.dlna.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.demo.DeviceAdapter.OnItemSelectedListener;
import com.android.cast.dlna.device.CastDevice;
import com.permissionx.guolindev.PermissionX;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private DeviceAdapter mDeviceAdapter;
    private RadioGroup mControlMode;
    private TextView mCastDeviceInfo;

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
        mCastDeviceInfo = findViewById(R.id.cast_device_info);

        mControlMode = findViewById(R.id.cast_type_group);
        mControlMode.setOnCheckedChangeListener(mOnCheckedChangeListener);

        RecyclerView recyclerView = findViewById(R.id.cast_device_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mDeviceAdapter = new DeviceAdapter(this, mOnClickListener));
        DLNACastManager.getInstance().addRegistryDeviceListener(mDeviceAdapter);
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
        showCastDeviceInformation(null);
        mDeviceAdapter.setSelectedDevice(null);
        DLNACastManager.getInstance().unbindCastService(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DLNACastManager.getInstance().removeRegistryListener(mDeviceAdapter);
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
            DLNACastManager.getInstance().clear();
            DLNACastManager.getInstance().search(DLNACastManager.DEVICE_TYPE_DMR, 60);
        } else if (item.getItemId() == R.id.menu_link_detail) {
            startActivity(new Intent(this, CastControlActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private final OnItemSelectedListener mOnClickListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(CastDevice castDevice, boolean selected) {
            mDeviceAdapter.setSelectedDevice(selected ? castDevice : null);

            if (mControlMode.getCheckedRadioButtonId() == R.id.cast_type_info) {
                if (selected) {
                    showCastDeviceInformation(castDevice);
                } else {
                    showCastDeviceInformation(null);
                }
            } else {
                if (selected) {
                    DLNACastManager.getInstance().connect(castDevice);
                    startActivity(new Intent(MainActivity.this, CastControlActivity.class));
                } else {
                    DLNACastManager.getInstance().disconnect();
                }
            }
        }
    };

    private final RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mDeviceAdapter.getCastDevice() != null) {
                if (checkedId == R.id.cast_type_ctrl) {
                    DLNACastManager.getInstance().connect(mDeviceAdapter.getCastDevice());
                    startActivity(new Intent(MainActivity.this, CastControlActivity.class));
                } else if (checkedId == R.id.cast_type_info) {
                    showCastDeviceInformation(mDeviceAdapter.getCastDevice());
                }
            }
        }
    };

    private void showCastDeviceInformation(@Nullable CastDevice castDevice) {
        if (castDevice == null) {
            mCastDeviceInfo.setText("");
            return;
        }

        StringBuilder builder = new StringBuilder();
        Device<?, ?, ?> device = castDevice.getDevice();
        if (device.getDetails().getBaseURL() != null) {
            builder.append("URL: ").append(device.getDetails().getBaseURL().toString()).append("\n");
        }
        builder.append("DeviceType: ").append(device.getType().getType()).append("\n");
        builder.append("ModelName: ").append(device.getDetails().getModelDetails().getModelName()).append("\n");
        builder.append("ModelDescription: ").append(device.getDetails().getModelDetails().getModelDescription()).append("\n");
        builder.append("ModelURL: ").append(device.getDetails().getModelDetails().getModelURI().toString()).append("\n");

        Service<?, ?>[] services = device.getServices();
        if (services != null) {
            for (Service<?, ?> service : services) {
                builder.append("\n");
                builder.append("ServiceId: ").append(service.getServiceId().getId()).append("\n");
                builder.append("ServiceType: ").append(service.getServiceType().getType()).append("\n");
                List<Action<?>> list = Arrays.asList(service.getActions());
                Collections.sort(list, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                builder.append("Action: ");
                for (Action<?> action : list) {
                    builder.append(action.getName()).append(", ");
                }
                builder.append("\n");
            }
        }

        mCastDeviceInfo.setText(builder.toString());
    }

}
