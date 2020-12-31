package com.android.cast.dlna.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.device.CastDevice;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InfoFragment extends Fragment implements IDisplayDevice {

    private TextView mCastDeviceInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initComponent(view);
    }

    private void initComponent(View view) {
        mCastDeviceInfo = view.findViewById(R.id.cast_device_info);
    }

    @Override
    public void setCastDevice(CastDevice castDevice) {
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
