package com.liuwei.android.demo.upnpcast;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.liuwei.android.demo.upnpcast.DeviceAdapter.OnItemSelectedListener;
import com.liuwei.android.upnpcast.NLUpnpCastManager;
import com.liuwei.android.upnpcast.controller.CastObject;
import com.liuwei.android.upnpcast.controller.ICastEventListener;
import com.liuwei.android.upnpcast.device.CastDevice;
import com.liuwei.android.upnpcast.util.CastUtils;
import com.liuwei.android.upnpcast.util.NetworkUtils;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity
{
    private DeviceAdapter mDeviceAdapter;
    private TextView mCastDeviceInfo;
    private TextView mCastMediaInfo;
    private TextView mCastStatusInfo;
    private TextView mCastPosition;
    private SeekBar mVolumeBar;
    private SeekBar mDurationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (com.liuwei.android.upnpcast.Constants.DEBUG)
        {
            Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);
        }

        initComponent();
    }

    private void initComponent()
    {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        getSupportActionBar().setTitle("DLNACast");

        mCastDeviceInfo = findViewById(R.id.cast_device_info);
        mCastMediaInfo = findViewById(R.id.cast_media_info);
        mCastStatusInfo = findViewById(R.id.cast_status_info);
        mCastPosition = findViewById(R.id.cast_position);
        ((TextView) findViewById(R.id.cast_network_info)).setText(NetworkUtils.getActiveNetworkInfo(this));

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

        NLUpnpCastManager.getInstance().addCastEventListener(mControlListener);
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
        NLUpnpCastManager.getInstance().disconnect();

        NLUpnpCastManager.getInstance().removeRegistryListener(mDeviceAdapter);

        NLUpnpCastManager.getInstance().removeCastEventListener(mControlListener);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_search_start:

                Toast.makeText(this, "开始搜索", Toast.LENGTH_SHORT).show();

                NLUpnpCastManager.getInstance().clear(); //TODO, need clear first?

                //NLUpnpCastManager.getInstance().search();
                NLUpnpCastManager.getInstance().search(NLUpnpCastManager.DEVICE_TYPE_DMR, 60);

                break;

            //            case R.id.menu_nlplayer:
            //
            //                Toast.makeText(this, "打开播放器", Toast.LENGTH_SHORT).show();
            //
            //                startActivity(new Intent(this, PlayerActivity.class));
            //
            //                break;
            //
            //            case R.id.menu_light:
            //
            //                startActivity(new Intent(this, LightActivity.class));
            //
            //                break;
            //
            //            case R.id.menu_browser:
            //
            //                startActivity(new Intent(this, BrowserActivity.class));
            //
            //                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private OnItemSelectedListener mOnClickListener = new OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(CastDevice castDevice, boolean selected)
        {
            if (selected)
            {
                mDeviceAdapter.setSelectedDevice(castDevice);

                NLUpnpCastManager.getInstance().connect(castDevice);

                mCastDeviceInfo.setText(String.format("当前设备：%s", castDevice.getName()));
            }
            else
            {
                mDeviceAdapter.setSelectedDevice(null);

                NLUpnpCastManager.getInstance().disconnect();

                mCastDeviceInfo.setText(String.format("当前设备: "));
            }
        }
    };

    private OnClickListener mControlClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_cast:

                    NLUpnpCastManager.getInstance().cast(
                            CastObject
                                    .newInstance(Constants.CAST_URL, Constants.CAST_ID, Constants.CAST_NAME)
                                    .setDuration(Constants.CAST_VIDEO_DURATION)
                    );

                    break;

                case R.id.btn_cast_stop:

                    NLUpnpCastManager.getInstance().stop();

                    break;

                case R.id.btn_cast_resume:

                    NLUpnpCastManager.getInstance().start();

                    break;

                case R.id.btn_cast_pause:

                    NLUpnpCastManager.getInstance().pause();

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

                    int position = (int) ((seekBar.getProgress() * 1f / seekBar.getMax()) * Constants.CAST_VIDEO_DURATION);

                    NLUpnpCastManager.getInstance().seekTo(position);

                    break;
            }
        }
    };

    // --------------------------------------------------------------------------------------------------------
    // Listener
    // --------------------------------------------------------------------------------------------------------
    private ICastEventListener mControlListener = new ICastEventListener()
    {
        @Override
        public void onConnecting(@NonNull CastDevice castDevice)
        {
            mCastDeviceInfo.setText(String.format("设备状态: [%s] [正在连接]", castDevice.getName()));

            Toast.makeText(MainActivity.this, "正在连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume)
        {
            mCastDeviceInfo.setText(String.format("设备状态: [%s] [已连接]", castDevice.getName()));

            mCastStatusInfo.setText(String.format("播放状态: [%s]", transportInfo.getCurrentTransportState().getValue()));

            mCastMediaInfo.setText(String.format("视频信息: [%s]", mediaInfo != null ? mediaInfo.getCurrentURI() : "NULL"));

            mVolumeBar.setProgress(volume);

            mDeviceAdapter.setSelectedDevice(castDevice);

            Toast.makeText(MainActivity.this, "已连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnect()
        {
            mCastDeviceInfo.setText(String.format("设备状态: [%s]", "断开连接"));

            mDeviceAdapter.setSelectedDevice(null);

            Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCast(CastObject castObject)
        {
            Toast.makeText(MainActivity.this, "开始投射 " + castObject.url, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart()
        {
            Toast.makeText(MainActivity.this, "开始播放", Toast.LENGTH_SHORT).show();

            mCastStatusInfo.setText(String.format("播放状态: [%s]", "开始播放"));
        }

        @Override
        public void onPause()
        {
            Toast.makeText(MainActivity.this, "暂停播放", Toast.LENGTH_SHORT).show();

            mCastStatusInfo.setText(String.format("播放状态: [%s]", "暂停播放"));
        }

        @Override
        public void onStop()
        {
            Toast.makeText(MainActivity.this, "停止投射", Toast.LENGTH_SHORT).show();

            //clear all UI
            {
                mCastStatusInfo.setText("播放状态: ");
                mCastMediaInfo.setText("视频信息: ");
                mDurationBar.setProgress(0);
                mCastPosition.setText("");
            }
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
        public void onVolume(int volume)
        {
            Toast.makeText(MainActivity.this, "音量：" + volume, Toast.LENGTH_SHORT).show();

            mVolumeBar.setProgress(volume);
        }

        @Override
        public void onBrightness(int brightness)
        {
            Toast.makeText(MainActivity.this, "亮度：" + brightness, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdatePositionInfo(PositionInfo positionInfo)
        {
            mCastPosition.setText(String.format("%s/%s", positionInfo.getRelTime(), positionInfo.getTrackDuration()));

            mDurationBar.setProgress((int) (positionInfo.getElapsedPercent() / 100f * mDurationBar.getMax()));
        }
    };
}
