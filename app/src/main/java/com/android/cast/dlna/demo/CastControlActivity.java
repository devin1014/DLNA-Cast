package com.android.cast.dlna.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.controller.CastObject;
import com.android.cast.dlna.controller.ICastEventListener;
import com.android.cast.dlna.device.CastDevice;
import com.android.cast.dlna.util.CastUtils;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class CastControlActivity extends AppCompatActivity {

    private TextView mCastDeviceInfo;
    private TextView mCastMediaInfo;
    private TextView mCastStatusInfo;
    private TextView mCastPosition;
    private ImageView mVolumeMute;
    private SeekBar mVolumeBar;
    private SeekBar mDurationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast_control);
        DLNACastManager.getInstance().bindCastService(this);
        initComponent();
    }

    private void initComponent() {
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            CastDevice castDevice = DLNACastManager.getInstance().getCastDevice();
            getSupportActionBar().setTitle(castDevice != null ? castDevice.getName() : "NULL");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        mCastDeviceInfo = findViewById(R.id.cast_device_info);
        mCastMediaInfo = findViewById(R.id.cast_media_info);
        mCastStatusInfo = findViewById(R.id.cast_status_info);
        mCastPosition = findViewById(R.id.cast_position);

        findViewById(R.id.btn_cast).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast_pause).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast_resume).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast_stop).setOnClickListener(mControlClickListener);

        mDurationBar = findViewById(R.id.seek_cast_duration);
        mDurationBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mVolumeBar = findViewById(R.id.seek_cast_volume);
        mVolumeBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mVolumeMute = findViewById(R.id.btn_volume_mute);
        mVolumeMute.setOnClickListener(mControlClickListener);
    }

    @Override
    protected void onDestroy() {
        DLNACastManager.getInstance().disconnect();
        DLNACastManager.getInstance().unbindCastService(this);
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    private final View.OnClickListener mControlClickListener = v -> {
        switch (v.getId()) {
            case R.id.btn_cast: {
                new CastFragment()
                        .setCallback(url -> DLNACastManager.getInstance().cast(CastObject.newInstance(url, Constants.CAST_ID, Constants.CAST_NAME)))
                        .show(getSupportFragmentManager(), "CastFragment");
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
            Toast.makeText(CastControlActivity.this, "正在连接", Toast.LENGTH_SHORT).show();
            mCastDeviceInfo.setText(String.format("设备状态: [%s] [正在连接]", castDevice.getName()));
        }

        @Override
        public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume) {
            Toast.makeText(CastControlActivity.this, "已连接", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(CastControlActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
            mCastDeviceInfo.setText(String.format("设备状态: [%s]", "断开连接"));
        }

        @Override
        public void onCast(CastObject castObject) {
            Toast.makeText(CastControlActivity.this, "开始投射 " + castObject.url, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart() {
            Toast.makeText(CastControlActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
            mCastStatusInfo.setText(String.format("播放状态: [%s]", "开始播放"));
        }

        @Override
        public void onPause() {
            Toast.makeText(CastControlActivity.this, "暂停播放", Toast.LENGTH_SHORT).show();
            mCastStatusInfo.setText(String.format("播放状态: [%s]", "暂停播放"));
        }

        @Override
        public void onStop() {
            Toast.makeText(CastControlActivity.this, "停止投射", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(CastControlActivity.this, "快进 " + CastUtils.getStringTime(position), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String errorMsg) {
            Toast.makeText(CastControlActivity.this, "错误：" + errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVolume(int volume) {
            Toast.makeText(CastControlActivity.this, "音量：" + volume, Toast.LENGTH_SHORT).show();
            mVolumeBar.setProgress(volume);
        }

        @Override
        public void onBrightness(int brightness) {
            Toast.makeText(CastControlActivity.this, "亮度：" + brightness, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdatePositionInfo(PositionInfo positionInfo) {
            mCastPosition.setText(String.format("%s/%s", positionInfo.getRelTime(), positionInfo.getTrackDuration()));
            mDurationBar.setProgress((int) (positionInfo.getElapsedPercent() / 100f * mDurationBar.getMax()));
        }
    };
}
