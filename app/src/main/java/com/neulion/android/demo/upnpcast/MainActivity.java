package com.neulion.android.demo.upnpcast;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.controller.CastObject;
import com.neulion.android.upnpcast.controller.ICastControlListener;
import com.neulion.android.upnpcast.device.CastDevice;
import com.neulion.android.upnpcast.util.CastUtils;
import com.neulion.android.upnpcast.util.NetworkUtils;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;


public class MainActivity extends AppCompatActivity
{
    private DeviceAdapter mDeviceAdapter;

    private TextView mCastInfo;

    private SeekBar mVolumeBar;

    private SeekBar mDurationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initComponent();
    }

    private void initComponent()
    {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mCastInfo = findViewById(R.id.cast_info);
        ((TextView) findViewById(R.id.cast_network_info)).setText(NetworkUtils.getActiveNetworkInfo(this));

        findViewById(R.id.btn_search).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast_pause).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast_resume).setOnClickListener(mControlClickListener);
        findViewById(R.id.btn_cast_stop).setOnClickListener(mControlClickListener);

        mDurationBar = findViewById(R.id.seek_cast_duration);
        mDurationBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mVolumeBar = findViewById(R.id.seek_cast_volume);
        mVolumeBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        RecyclerView recyclerView = findViewById(R.id.cast_device_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mDeviceAdapter = new DeviceAdapter(this, mOnClickListener));

        NLUpnpCastManager.getInstance().setOnControlListener(mControlListener);
        NLUpnpCastManager.getInstance().addRegistryDeviceListener(mDeviceAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        NLUpnpCastManager.getInstance().bindUpnpCastService(this);
    }

    @Override
    protected void onPause()
    {
        NLUpnpCastManager.getInstance().unbindUpnpCastService(this);

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        NLUpnpCastManager.getInstance().removeRegistryListener(mDeviceAdapter);

        super.onDestroy();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v)
        {
            CastDevice castDevice = (CastDevice) v.getTag();

            if (castDevice != null)
            {
                mDeviceAdapter.setSelectedDevice(castDevice);

                NLUpnpCastManager.getInstance().connect(castDevice);

                mCastInfo.setText("当前设备：" + castDevice.getName());
            }
        }
    };

    private static final String CAST_URL_LOCAL_TEST = "http://172.16.0.107:8506/clear/teststage/t594_hd_apptv.m3u8";
    private static final String CAST_URL_IPHONE_SAMPLE = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";

    private static final String CAST_ID = "101";

    private static final String CAST_NAME = "castDemo";

    private static final int CAST_VIDEO_DURATION = 30 * 60 * 1000;

    private OnClickListener mControlClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_search:

                    NLUpnpCastManager.getInstance().search(NLUpnpCastManager.DEVICE_TYPE_DMR);

                    break;

                case R.id.btn_cast:

                    NLUpnpCastManager.getInstance().cast(CastObject.newInstance(CAST_URL_IPHONE_SAMPLE, CAST_ID, CAST_NAME, CAST_VIDEO_DURATION));

                    break;

                case R.id.btn_cast_resume:

                    NLUpnpCastManager.getInstance().start();

                    break;

                case R.id.btn_cast_pause:

                    NLUpnpCastManager.getInstance().pause();

                    break;

                case R.id.btn_cast_stop:

                    NLUpnpCastManager.getInstance().stop();

                    break;
            }
        }
    };

    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            switch (seekBar.getId())
            {
                case R.id.seek_cast_volume:

                    NLUpnpCastManager.getInstance().setVolume((int) (seekBar.getProgress() * 100f / seekBar.getMax()));

                    break;

                case R.id.seek_cast_duration:

                    int position = (int) ((seekBar.getProgress() * 1f / seekBar.getMax()) * CAST_VIDEO_DURATION);

                    NLUpnpCastManager.getInstance().seekTo(position);

                    break;
            }
        }
    };

    private ICastControlListener mControlListener = new ICastControlListener()
    {
        @Override
        public void onOpen(String url)
        {
            Toast.makeText(MainActivity.this, "开始投射 " + url, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart()
        {
            Toast.makeText(MainActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause()
        {
            Toast.makeText(MainActivity.this, "暂停播放", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStop()
        {
            Toast.makeText(MainActivity.this, "停止投射", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSeekTo(long position)
        {
            Toast.makeText(MainActivity.this, "快进 " + CastUtils.getStringTime(position), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String errorMsg)
        {
            Toast.makeText(MainActivity.this, "错误：" + errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVolume(long volume)
        {
            Toast.makeText(MainActivity.this, "音量：" + volume, Toast.LENGTH_SHORT).show();
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onSyncMediaInfo(CastDevice castDevice, MediaInfo mediaInfo)
        {
            if (castDevice != null)
            {
                mDeviceAdapter.setSelectedDevice(castDevice);

                mCastInfo.setText("当前设备: " + castDevice.getName());

                if (mediaInfo != null)
                {
                    mCastInfo.append("\n\n视频信息:");
                    mCastInfo.append("\nCurrentURI：" + mediaInfo.getCurrentURI());
                    //mCastInfo.append("\nMetaData：" + mediaInfo.getCurrentURIMetaData());
                    mCastInfo.append("\nMediaDuration：" + mediaInfo.getMediaDuration());
                }
            }
        }

        @Override
        public void onMediaPositionInfo(PositionInfo positionInfo)
        {
            mDurationBar.setProgress((int) (positionInfo.getElapsedPercent() / 100f * mDurationBar.getMax()));
        }
    };
}
