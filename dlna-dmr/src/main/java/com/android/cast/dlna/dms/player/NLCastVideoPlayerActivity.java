package com.android.cast.dlna.dms.player;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.cast.dlna.dms.Constants.Key;
import com.android.cast.dlna.dms.DLNARendererService;
import com.android.cast.dlna.dms.DLNARendererService.RendererServiceBinder;
import com.android.cast.dlna.dms.CastUtils;
import com.android.cast.dlna.dms.ILogger;
import com.android.cast.dlna.dms.ILogger.DefaultLoggerImpl;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.net.URI;

/**
 *
 */
public class NLCastVideoPlayerActivity extends AppCompatActivity implements RendererThread.IActivityAliveCallback {
    private final UnsignedIntegerFourBytes INSTANCE_ID = new UnsignedIntegerFourBytes(0);
    private ILogger mLogger = new DefaultLoggerImpl(this);
    // private CommonVideoController mVideoController;
    private NLCastMediaController mCastControlImp;
    private URI mURI;
    private boolean mActivityDestroy = false;
    private DLNARendererService mRendererService;
    private ServiceConnection mRendererServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLogger.i(String.format("onServiceConnected: [%s]", name.getShortClassName()));

            mRendererService = ((RendererServiceBinder) service).getRendererService();

            mRendererService.registerControlBridge(mCastControlImp = new NLCastMediaController(NLCastVideoPlayerActivity.this, mRendererService));

            //TODO: service maybe bind more than once!
            new RendererThread.AvControlThread(NLCastVideoPlayerActivity.this, mRendererService).start();

            new RendererThread.AudioControlThread(NLCastVideoPlayerActivity.this, mRendererService).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLogger.w(String.format("onServiceDisconnected: [%s]", name.getShortClassName()));

            mRendererService = null;
        }
    };
    private AudioManager mAudioManager;

    public static void startActivity(Context context, CastMediaRequest mediaRequest) {
        Intent intent = new Intent(context, NLCastVideoPlayerActivity.class);

        intent.putExtra(Key.EXTRA_CAST_REQUEST, mediaRequest);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger.d("onCreate: " + getIntent());

        mActivityDestroy = false;

        // setContentView(R.layout.activity_cast_video_player);
        //
        // mVideoController = findViewById(R.id.d_video_controller);
        //
        // ControlBar topControlBar = mVideoController.findViewById(R.id.m_top_bar_panel);
        //
        // topControlBar.setSupported(false);
        //
        // mVideoController.setSupportFullScreenControls(false);
        //
        // mVideoController.setFullScreen(true);

        bindService(new Intent(this, DLNARendererService.class), mRendererServiceConnection, Service.BIND_AUTO_CREATE);

        openMedia(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mLogger.d("onNewIntent: " + intent);

        openMedia(intent);
    }

    private void openMedia(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            CastMediaRequest mediaRequest = bundle.getParcelable(Key.EXTRA_CAST_REQUEST);

            //noinspection ConstantConditions
            mURI = CastUtils.parseURI(mediaRequest.videoURL);
        }

        if (mURI != null) {
            mLogger.i("open Media: " + mURI.toString());

            // mVideoController.openMedia(mURI.toString());

            if (mRendererService != null) {
                mRendererService.getAvTransportLastChange()

                        .setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.NO_MEDIA_PRESENT));
            }
        } else {
            mLogger.i("URI is NULL!");

            finish();
        }
    }

    @Override
    protected void onDestroy() {
        mLogger.w("onDestroy");

        // if (mVideoController != null) {
        //     mVideoController.releaseMedia();
        // }

        if (mRendererService != null) {
            mRendererService.unregisterControlBridge(mCastControlImp);
        }

        unbindService(mRendererServiceConnection);

        super.onDestroy();

        mActivityDestroy = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
            }

            @SuppressWarnings("ConstantConditions") int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            mRendererService.getAudioControlLastChange().setEventedValue(INSTANCE_ID, new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, volume)));
        }

        return handled;
    }

    @Override
    public boolean isActivityDestroyed() {
        return mActivityDestroy;
    }
}
