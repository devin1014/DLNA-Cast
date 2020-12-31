package com.android.cast.dlna.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.controller.ICastEventListener;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.CastUtils;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class ControlFragment extends Fragment implements IDisplayDevice {

    private TextView mCastDeviceInfo;
    private TextView mCastMediaInfo;
    private TextView mCastStatusInfo;
    private TextView mCastPosition;
    private ImageView mVolumeMute;
    private SeekBar mVolumeBar;
    private SeekBar mDurationBar;

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
        mCastDeviceInfo = view.findViewById(R.id.cast_device_info);
        mCastMediaInfo = view.findViewById(R.id.cast_media_info);
        mCastStatusInfo = view.findViewById(R.id.cast_status_info);
        mCastPosition = view.findViewById(R.id.cast_position);

        view.findViewById(R.id.btn_cast).setOnClickListener(mControlClickListener);
        view.findViewById(R.id.btn_cast_pause).setOnClickListener(mControlClickListener);
        view.findViewById(R.id.btn_cast_resume).setOnClickListener(mControlClickListener);
        view.findViewById(R.id.btn_cast_stop).setOnClickListener(mControlClickListener);

        mDurationBar = view.findViewById(R.id.seek_cast_duration);
        mDurationBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mVolumeBar = view.findViewById(R.id.seek_cast_volume);
        mVolumeBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mVolumeMute = view.findViewById(R.id.btn_volume_mute);
        mVolumeMute.setOnClickListener(mControlClickListener);
    }

    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener mControlClickListener = v -> {
        switch (v.getId()) {
            case R.id.btn_cast: {
                new CastFragment()
                        .setCallback(url -> DLNACastManager.getInstance().cast(CastObject.newInstance(url, Constants.CAST_ID, Constants.CAST_NAME)))
                        .show(getChildFragmentManager(), "CastFragment");
                break;
            }
            case R.id.btn_cast_stop: {
                DLNACastManager.getInstance().stop();
                break;
            }
            case R.id.btn_cast_resume: {
                DLNACastManager.getInstance().start();
                break;
            }
            case R.id.btn_cast_pause: {
                DLNACastManager.getInstance().pause();
                break;
            }
            case R.id.btn_volume_mute: {
                DLNACastManager.getInstance().setMute(true); //TODO, always true?
                break;
            }
        }
    };

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
                case R.id.seek_cast_volume: {
                    DLNACastManager.getInstance().setVolume((int) (seekBar.getProgress() * 100f / seekBar.getMax()));
                    break;
                }
                case R.id.seek_cast_duration: {
                    int position = (int) ((seekBar.getProgress() * 1f / seekBar.getMax()) * Constants.CAST_VIDEO_DURATION);
                    DLNACastManager.getInstance().seekTo(position);
                    break;
                }
            }
        }
    };
    // --------------------------------------------------------------------------------------------------------
    // Listener
    // --------------------------------------------------------------------------------------------------------
    private final ICastEventListener mControlListener = new ICastEventListener() {
        @Override
        public void onConnecting(@NonNull CastDevice castDevice) {
            Toast.makeText(getContext(), "正在连接", Toast.LENGTH_SHORT).show();
            mCastDeviceInfo.setText(String.format("设备状态: [%s] [正在连接]", castDevice.getName()));
        }

        @Override
        public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume) {
            Toast.makeText(getContext(), "已连接", Toast.LENGTH_SHORT).show();
            mCastDeviceInfo.setText(String.format("设备状态: [%s] [已连接]", castDevice.getName()));
            mCastStatusInfo.setText(String.format("播放状态: [%s]", transportInfo.getCurrentTransportState().getValue()));
            mCastMediaInfo.setText(String.format("视频信息: [%s]", mediaInfo != null ? mediaInfo.getCurrentURI() : "NULL"));
            mVolumeBar.setProgress(volume);

            boolean getMute = castDevice.supportAction("GetMute");
            mVolumeMute.setEnabled(getMute);
            mVolumeMute.setImageResource(getMute ? R.drawable.baseline_volume_mute : R.drawable.baseline_volume_off);
            mVolumeBar.setEnabled(getMute);
        }

        @Override
        public void onDisconnect() {
            Toast.makeText(getContext(), "断开连接", Toast.LENGTH_SHORT).show();
            mCastDeviceInfo.setText(String.format("设备状态: [%s]", "断开连接"));
        }

        @Override
        public void onCast(CastObject castObject) {
            Toast.makeText(getContext(), "开始投射 " + castObject.url, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart() {
            Toast.makeText(getContext(), "开始播放", Toast.LENGTH_SHORT).show();
            mCastStatusInfo.setText(String.format("播放状态: [%s]", "开始播放"));
        }

        @Override
        public void onPause() {
            Toast.makeText(getContext(), "暂停播放", Toast.LENGTH_SHORT).show();
            mCastStatusInfo.setText(String.format("播放状态: [%s]", "暂停播放"));
        }

        @Override
        public void onStop() {
            Toast.makeText(getContext(), "停止投射", Toast.LENGTH_SHORT).show();
            //clear all UI
            {
                mCastStatusInfo.setText("播放状态: ");
                mCastMediaInfo.setText("视频信息: ");
                mDurationBar.setProgress(0);
                mCastPosition.setText("");
            }
        }

        @Override
        public void onSeekTo(long position) {
            Toast.makeText(getContext(), "快进 " + CastUtils.getStringTime(position), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String errorMsg) {
            Toast.makeText(getContext(), "错误：" + errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVolume(int volume) {
            Toast.makeText(getContext(), "音量：" + volume, Toast.LENGTH_SHORT).show();
            mVolumeBar.setProgress(volume);
        }

        @Override
        public void onBrightness(int brightness) {
            Toast.makeText(getContext(), "亮度：" + brightness, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdatePositionInfo(PositionInfo positionInfo) {
            mCastPosition.setText(String.format("%s/%s", positionInfo.getRelTime(), positionInfo.getTrackDuration()));
            mDurationBar.setProgress((int) (positionInfo.getElapsedPercent() / 100f * mDurationBar.getMax()));
        }
    };

    @Override
    public void setCastDevice(CastDevice device) {
        //TODO
    }
}
