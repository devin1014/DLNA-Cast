package com.android.cast.dlna.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.device.CastDevice;

public class ControlFragment extends Fragment implements IDisplayDevice {

    private TextView mDeviceStatus;
    private TextView mPositionInfo;
    private SeekBar mPositionSeekBar;
    private TextView mVolumeInfo;
    private SeekBar mVolumeSeekBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
    }

    private void initComponent(View view) {
        mDeviceStatus = view.findViewById(R.id.ctrl_device_status);
        mPositionInfo = view.findViewById(R.id.ctrl_position_info);

        view.findViewById(R.id.btn_cast).setOnClickListener(v ->
                new CastFragment()
                        .setCallback(url -> DLNACastManager.getInstance().cast(CastObject.newInstance(url, Constants.CAST_ID, Constants.CAST_NAME)))
                        .show(getChildFragmentManager(), "CastFragment"));
        view.findViewById(R.id.btn_cast_pause).setOnClickListener(v -> DLNACastManager.getInstance().pause());
        view.findViewById(R.id.btn_cast_resume).setOnClickListener(v -> DLNACastManager.getInstance().start());
        view.findViewById(R.id.btn_cast_stop).setOnClickListener(v -> DLNACastManager.getInstance().stop());
        view.findViewById(R.id.btn_cast_mute).setOnClickListener(v -> DLNACastManager.getInstance().setMute(true));

        mPositionInfo = view.findViewById(R.id.ctrl_position_info);
        mPositionSeekBar = view.findViewById(R.id.ctrl_seek_position);
        mPositionSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mVolumeInfo = view.findViewById(R.id.ctrl_volume_info);
        mVolumeSeekBar = view.findViewById(R.id.ctrl_seek_volume);
        mVolumeSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            switch (seekBar.getId()) {
                case R.id.ctrl_seek_volume: {
                    DLNACastManager.getInstance().setVolume((int) (seekBar.getProgress() * 100f / seekBar.getMax()));
                    break;
                }
                case R.id.ctrl_seek_position: {
                    int position = (int) ((seekBar.getProgress() * 1f / seekBar.getMax()) * Constants.CAST_VIDEO_DURATION);
                    DLNACastManager.getInstance().seekTo(position);
                    break;
                }
            }
        }
    };

    @Override
    public void setCastDevice(CastDevice device) {
        if (device == null) DLNACastManager.getInstance().disconnect();
        else DLNACastManager.getInstance().connect(device);
    }
}
