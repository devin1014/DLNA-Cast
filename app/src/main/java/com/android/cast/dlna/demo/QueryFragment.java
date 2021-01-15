package com.android.cast.dlna.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.DLNACastManager;

import org.fourthline.cling.model.meta.Device;

public class QueryFragment extends Fragment implements IDisplayDevice {

    private TextView mMediaInfo;
    private TextView mPositionInfo;
    private TextView mTransportInfo;
    private TextView mVolumeInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_query, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
    }

    private void initComponent(View view) {
        mMediaInfo = view.findViewById(R.id.ctrl_device_query_media_info);
        mPositionInfo = view.findViewById(R.id.ctrl_device_query_position_info);
        mTransportInfo = view.findViewById(R.id.ctrl_device_query_transport_info);
        mVolumeInfo = view.findViewById(R.id.ctrl_device_query_volume_info);
        view.findViewById(R.id.ctrl_device_query_refresh).setOnClickListener((v -> setInfo()));
    }

    private Device<?, ?, ?> mDevice;

    @Override
    public void setCastDevice(Device<?, ?, ?> device) {
        mDevice = device;
        if (!isHidden()) setInfo();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) setInfo();
    }

    private void setInfo() {
        mMediaInfo.setText("");
        mPositionInfo.setText("");
        mTransportInfo.setText("");
        mVolumeInfo.setText("");

        if (mDevice != null) {
            DLNACastManager.getInstance().getMediaInfo(mDevice, (mediaInfo, errMsg) ->
                    mMediaInfo.setText(String.format("MediaInfo:\n%s", mediaInfo != null ? mediaInfo.getCurrentURI() : errMsg)));

            DLNACastManager.getInstance().getPositionInfo(mDevice, (positionInfo, errMsg) -> {
                try {
                    mPositionInfo.setText(String.format("PositionInfo:\n%s", positionInfo != null ? positionInfo : errMsg));
                } catch (Exception e) {
                    e.printStackTrace();
                    mPositionInfo.setText(e.toString());
                }
            });

            DLNACastManager.getInstance().getTransportInfo(mDevice, (transportInfo, errMsg) ->
                    mTransportInfo.setText(String.format("TransportInfo:\n%s", transportInfo != null ? transportInfo.getCurrentTransportState() : errMsg)));

            DLNACastManager.getInstance().getVolumeInfo(mDevice, (volume, errMsg) ->
                    mVolumeInfo.setText(String.format("Volume: %s", volume != null ? volume : errMsg)));
        }
    }
}
