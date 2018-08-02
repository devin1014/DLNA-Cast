package com.neulion.android.demo.player;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.neulion.android.demo.upnpcast.Constants;
import com.neulion.android.demo.upnpcast.DeviceAdapter;
import com.neulion.android.demo.upnpcast.DeviceAdapter.OnItemSelectedListener;
import com.neulion.android.demo.upnpcast.R;
import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.controller.CastObject;
import com.neulion.android.upnpcast.controller.ICastEventListener;
import com.neulion.android.upnpcast.device.CastDevice;
import com.neulion.android.upnpcast.util.CastUtils;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.MediaControl.OnRequestRestartListener;
import com.neulion.media.control.MediaRequest;
import com.neulion.media.control.impl.CommonVideoController;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

public class PlayerActivity extends AppCompatActivity
{
    private ILogger mLogger = new DefaultLoggerImpl(this);
    private CommonVideoController mController;
    private DeviceAdapter mDeviceAdapter;
    private NLCastMediaConnection mCastMediaConnection;
    private View mCastingView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_player);

        initComponent();

        NLUpnpCastManager.getInstance().addCastEventListener(mControlListener);

        NLUpnpCastManager.getInstance().addRegistryDeviceListener(mDeviceAdapter = new DeviceAdapter(this, mOnItemSelectedListener));

        openMedia(Constants.CAST_URL, 0L);
    }

    private void initComponent()
    {
        mCastingView = findViewById(R.id.d_video_cast_view);

        mController = findViewById(R.id.d_video_controller);

        mController.setFullScreen(false);

        mController.setSupportFullScreenControls(true);

        mController.removeSupportedGestures(CommonVideoController.GESTURE_SCROLL_VERTICAL_VOLUME);

        mController.setMediaConnection(mCastMediaConnection = new NLCastMediaConnection(this));

        mController.setOnRequestRestartListener(mOnRequestRestartListener);

        mCastMediaConnection.setEnabled(NLUpnpCastManager.getInstance().isConnected());

        ViewGroup viewGroup = mController.findViewById(R.id.m_controller_fit_system_windows_panel);

        Button button = CastButtonFactory.newCastButton(this, viewGroup);

        button.setOnClickListener(mCastOnClickListener);

        viewGroup.addView(button);
    }

    private void openMedia(String url, Long seekPosition)
    {
        mLogger.i(String.format("openMedia[%s]", url));

        if (mController != null)
        {
            final MediaRequest request = new MediaRequest(url);

            // open media.
            mController.openMedia(request);

            if (seekPosition != null && seekPosition > 0)
            {
                mController.seekTo(seekPosition);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mController != null && mController.isFullScreen())
        {
            mController.setFullScreen(false);

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        NLUpnpCastManager.getInstance().bindUpnpCastService(PlayerActivity.this);
    }

    @Override
    protected void onPause()
    {
        NLUpnpCastManager.getInstance().unbindUpnpCastService(PlayerActivity.this);

        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        NLUpnpCastManager.getInstance().removeCastEventListener(mControlListener);

        NLUpnpCastManager.getInstance().removeRegistryListener(mDeviceAdapter);

        if (!NLUpnpCastManager.getInstance().isConnected())
        {
            mController.releaseMedia();
        }

        super.onDestroy();
    }

    private OnRequestRestartListener mOnRequestRestartListener = new OnRequestRestartListener()
    {
        @Override
        public boolean onRequestRestart(Long seekPosition)
        {
            mLogger.w("----------------------------------------------------------------------------");
            mLogger.w(String.format("onRequestRestart: [%s ms]", seekPosition != null ? seekPosition : 0));
            mLogger.w("----------------------------------------------------------------------------");

            boolean connected = NLUpnpCastManager.getInstance().isConnected();

            mCastingView.setVisibility(connected ? View.VISIBLE : View.GONE);

            mController.releaseMedia();

            final long position = seekPosition == null ? 0L : seekPosition;

            openMedia(Constants.CAST_URL, position);

            return true;
        }
    };

    private OnClickListener mCastOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            NLUpnpCastManager.getInstance().search(NLUpnpCastManager.DEVICE_TYPE_DMR, 60);

            Context context = PlayerActivity.this;

            AlertDialog.Builder builder = new Builder(context);

            RecyclerView recyclerView = new RecyclerView(context);

            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            recyclerView.setAdapter(mDeviceAdapter);

            builder.setView(recyclerView);

            builder.show();
        }
    };

    private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(CastDevice castDevice, boolean selected)
        {
            if (selected)
            {
                mDeviceAdapter.setSelectedDevice(castDevice);

                NLUpnpCastManager.getInstance().connect(castDevice);
            }
            else
            {
                mDeviceAdapter.setSelectedDevice(null);

                NLUpnpCastManager.getInstance().disconnect();
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
            Toast.makeText(getApplication(), "正在连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnected(@NonNull CastDevice castDevice, @NonNull TransportInfo transportInfo, @Nullable MediaInfo mediaInfo, int volume)
        {
            mCastMediaConnection.setEnabled(true);

            Toast.makeText(getApplication(), "已连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnect()
        {
            mCastMediaConnection.setEnabled(false);

            Toast.makeText(getApplication(), "断开连接", Toast.LENGTH_SHORT).show();

            mDeviceAdapter.setSelectedDevice(null);
        }

        @Override
        public void onCast(CastObject castObject)
        {
            if (castObject.getPosition() > 0)
            {
                NLUpnpCastManager.getInstance().seekTo(castObject.getPosition());
            }

            Toast.makeText(getApplication(), "开始投射 " + castObject.url, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart()
        {
            Toast.makeText(getApplication(), "开始播放", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause()
        {
            Toast.makeText(getApplication(), "暂停播放", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStop()
        {
            Toast.makeText(getApplication(), "停止投射", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSeekTo(long position)
        {
            Toast.makeText(getApplication(), "快进 " + CastUtils.getStringTime(position), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String errorMsg)
        {
            Toast.makeText(getApplication(), "错误：" + errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVolume(int volume)
        {
            Toast.makeText(getApplication(), "音量：" + volume, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBrightness(int brightness)
        {
            Toast.makeText(getApplication(), "亮度：" + brightness, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdatePositionInfo(PositionInfo positionInfo)
        {
        }
    };
}
