package com.android.cast.dlna.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.android.cast.dlna.Utils;
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

        DLNACastManager.getInstance().registerActionCallbacks(
                new ICastInterface.CastEventListener() {
                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(getActivity(), "Cast: " + result, Toast.LENGTH_SHORT).show();
                        mCircleMsgHandler.start(0);
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }
                },
                new ICastInterface.PlayEventListener() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getActivity(), "Play", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }
                },
                new ICastInterface.PauseEventListener() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getActivity(), "Pause", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }
                },
                new ICastInterface.StopEventListener() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(getActivity(), "Stop", Toast.LENGTH_SHORT).show();
                        mCircleMsgHandler.stop();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }
                },
                new ICastInterface.SeekToEventListener() {
                    @Override
                    public void onSuccess(Long result) {
                        Toast.makeText(getActivity(), "SeekTo: " + Utils.getStringTime(result), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
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

    @Override
    public void onDestroyView() {
        DLNACastManager.getInstance().unregisterActionCallbacks();
        super.onDestroyView();
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
                    if (mDurationMillSeconds > 0) {
                        int position = (int) ((seekBar.getProgress() * 1f / seekBar.getMax()) * mDurationMillSeconds);
                        DLNACastManager.getInstance().seekTo(position);
                    }
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

    private long mDurationMillSeconds = 0;

    private final Runnable mRefreshUIRunnable = () -> {
        if (mDevice == null) return;
        DLNACastManager.getInstance().getPositionInfo(mDevice, (positionInfo, errMsg) -> {
            if (positionInfo != null) {
                mPositionInfo.setText(String.format("%s:%s", positionInfo.getRelTime(), positionInfo.getTrackDuration()));
                if (positionInfo.getTrackDurationSeconds() != 0) {
                    mDurationMillSeconds = positionInfo.getTrackDurationSeconds() * 1000;
                    mPositionSeekBar.setProgress((int) (positionInfo.getTrackElapsedSeconds() * 100 / positionInfo.getTrackDurationSeconds()));
                } else {
                    mPositionSeekBar.setProgress(0);
                }
            } else {
                mPositionInfo.setText(errMsg);
            }
        });

    };

    private final CircleMessageHandler mCircleMsgHandler = new CircleMessageHandler(1000, mRefreshUIRunnable);

    private static class CircleMessageHandler extends Handler {
        private static final int MSG = 101;

        private final long duration;
        private final Runnable runnable;

        public CircleMessageHandler(long duration, @NonNull Runnable runnable) {
            super(Looper.getMainLooper());
            this.duration = duration;
            this.runnable = runnable;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            runnable.run();
            sendEmptyMessageDelayed(MSG, duration);
        }

        public void start(long delay) {
            stop();
            sendEmptyMessageDelayed(MSG, delay);
        }

        public void stop() {
            removeMessages(MSG);
        }
    }
}
