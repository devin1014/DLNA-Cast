package com.android.cast.dlna.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.control.ICastInterface;

import org.fourthline.cling.model.meta.Device;

public class ControlFragment extends Fragment implements IDisplayDevice, CastFragment.Callback {

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

        DLNACastManager.getInstance().registerActionCallback(new ICastInterface.CastEventListener() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(getActivity(), "Cast: " + result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errMsg) {
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });

        DLNACastManager.getInstance().registerActionCallback(new ICastInterface.PlayEventListener() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getActivity(), "Play", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errMsg) {
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });

        DLNACastManager.getInstance().registerActionCallback(new ICastInterface.PauseEventListener() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getActivity(), "Pause", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errMsg) {
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });

        DLNACastManager.getInstance().registerActionCallback(new ICastInterface.StopEventListener() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getActivity(), "Stop", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errMsg) {
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });

        DLNACastManager.getInstance().registerActionCallback(new ICastInterface.SeekToEventListener() {
            @Override
            public void onSuccess(Long result) {
                Toast.makeText(getActivity(), "SeekTo: " + result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errMsg) {
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initComponent(View view) {
        mPositionInfo = view.findViewById(R.id.ctrl_position_info);

        view.findViewById(R.id.btn_cast).setOnClickListener(v -> new CastFragment().setCallback(this).show(getChildFragmentManager(), "CastFragment"));
        view.findViewById(R.id.btn_cast_pause).setOnClickListener(v -> DLNACastManager.getInstance().pause());
        view.findViewById(R.id.btn_cast_resume).setOnClickListener(v -> DLNACastManager.getInstance().play());
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

    private Device<?, ?, ?> mDevice;

    @Override
    public void setCastDevice(Device<?, ?, ?> device) {
        mDevice = device;
    }

    @Override
    public void onCastUrl(String url) {
        if (mDevice != null) {
            DLNACastManager.getInstance().cast(mDevice, CastObject.newInstance(url, Constants.CAST_ID, Constants.CAST_NAME));
        }
    }
}
