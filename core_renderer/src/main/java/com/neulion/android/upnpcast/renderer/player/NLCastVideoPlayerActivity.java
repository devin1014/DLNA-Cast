package com.neulion.android.upnpcast.renderer.player;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.neulion.android.upnpcast.renderer.Constants.Key;
import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;
import com.neulion.android.upnpcast.renderer.NLUpnpRendererService.RendererServiceBinder;
import com.neulion.android.upnpcast.renderer.R;
import com.neulion.android.upnpcast.renderer.player.RendererThread.AudioControlThread;
import com.neulion.android.upnpcast.renderer.player.RendererThread.AvControlThread;
import com.neulion.android.upnpcast.renderer.utils.CastUtils;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.VideoController.ControlBar;
import com.neulion.media.control.impl.CommonVideoController;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.net.URI;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-25
 * Time: 11:03
 */
public class NLCastVideoPlayerActivity extends AppCompatActivity
{
    public static void startActivity(Context context, String url, String metaData)
    {
        Intent intent = new Intent(context, NLCastVideoPlayerActivity.class);

        intent.putExtra(Key.EXTRA_URL, url);

        intent.putExtra(Key.EXTRA_METADATA, metaData);

        context.startActivity(intent);
    }

    private final UnsignedIntegerFourBytes INSTANCE_ID = new UnsignedIntegerFourBytes(0);

    private ILogger mLogger = new DefaultLoggerImpl(this);

    private CommonVideoController mVideoController;

    private CastControlImp mCastControlImp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mLogger.d("onCreate: " + getIntent());

        setContentView(R.layout.activity_cast_video_player);

        mVideoController = findViewById(R.id.d_video_controller);

        ControlBar topControlBar = mVideoController.findViewById(R.id.m_top_bar_panel);

        topControlBar.setSupported(false);

        mVideoController.setSupportFullScreenControls(false);

        mVideoController.setFullScreen(true);

        bindService(new Intent(this, NLUpnpRendererService.class), mRendererServiceConnection, Service.BIND_AUTO_CREATE);

        openMedia(getIntent());

        mDestroy = false;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        mLogger.d("onNewIntent: " + intent);

        openMedia(intent);
    }

    private URI mURI;

    private void openMedia(Intent intent)
    {
        Uri uri = intent.getData();

        if (uri != null)
        {
            mURI = CastUtils.parseURI(uri);
        }
        else
        {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null)
            {
                String url = bundle.getString(Key.EXTRA_URL);

                mURI = CastUtils.parseURI(url);
            }
        }

        if (mURI != null)
        {
            mLogger.i("open Media: " + mURI.toString());

            mVideoController.openMedia(mURI.toString());

            if (mRendererService != null)
            {
                mRendererService.getAvTransportLastChange()

                        .setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.NO_MEDIA_PRESENT));
            }
        }
        else
        {
            mLogger.i("URI is NULL!");

            finish();
        }
    }

    private boolean mDestroy = false;

    public boolean isDestroy()
    {
        return mDestroy;
    }

    @Override
    protected void onDestroy()
    {
        mLogger.w("onDestroy");

        if (mVideoController != null)
        {
            mVideoController.releaseMedia();
        }

        if (mRendererService != null)
        {
            mRendererService.unregisterControlBridge(mCastControlImp);
        }

        unbindService(mRendererServiceConnection);

        super.onDestroy();

        mDestroy = true;
    }

    private NLUpnpRendererService mRendererService;

    private ServiceConnection mRendererServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mLogger.i(String.format("onServiceConnected: [%s]", name.getShortClassName()));

            mRendererService = ((RendererServiceBinder) service).getRendererService();

            mRendererService.registerControlBridge(mCastControlImp = new CastControlImp(NLCastVideoPlayerActivity.this, mRendererService, mVideoController));

            //TODO
            {
                new AvControlThread(NLCastVideoPlayerActivity.this, mRendererService, mVideoController).start();

                new AudioControlThread(NLCastVideoPlayerActivity.this, mRendererService, mVideoController).start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mLogger.w(String.format("onServiceDisconnected: [%s]", name.getShortClassName()));

            mRendererService = null;
        }
    };

    private AudioManager mAudioManager;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean handled = super.onKeyDown(keyCode, event);

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
        {
            if (mAudioManager == null)
            {
                mAudioManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
            }

            int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            mRendererService.getAudioControlLastChange().setEventedValue(INSTANCE_ID, new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, volume)));
        }

        return handled;
    }
}
