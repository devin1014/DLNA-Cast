package com.android.cast.dlna.dmr.player;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.cast.dlna.dmr.Constants.Key;
import com.android.cast.dlna.dmr.DLNARendererService;
import com.android.cast.dlna.dmr.DLNARendererService.RendererServiceBinder;
import com.android.cast.dlna.dmr.R;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

/**
 *
 */
public class DLNARendererActivity extends AppCompatActivity implements RendererThread.IActivityAliveCallback {

    public static void startActivity(Context context, CastMediaRequest mediaRequest) {
        Intent intent = new Intent(context, DLNARendererActivity.class);
        intent.putExtra(Key.EXTRA_CAST_REQUEST, mediaRequest);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private final UnsignedIntegerFourBytes INSTANCE_ID = new UnsignedIntegerFourBytes(0);
    private VideoView mVideoView;
    private ProgressBar mProgressBar;
    private CastMediaController mCastControlImp;
    private boolean mActivityDestroy = false;
    private DLNARendererService mRendererService;
    private AudioManager mAudioManager;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRendererService = ((RendererServiceBinder) service).getRendererService();
            mRendererService.registerControlBridge(mCastControlImp = new CastMediaController(DLNARendererActivity.this, mRendererService));
            //TODO: service maybe bind more than once!
            new RendererThread.AvControlThread(DLNARendererActivity.this, mRendererService).start();
            new RendererThread.AudioControlThread(DLNARendererActivity.this, mRendererService).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRendererService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna_renderer);
        mAudioManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
        bindService(new Intent(this, DLNARendererService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        mVideoView = findViewById(R.id.video_view);
        mProgressBar = findViewById(R.id.video_progress);
        mActivityDestroy = false;
        openMedia(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openMedia(intent);
    }

    private void openMedia(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            CastMediaRequest mediaRequest = bundle.getParcelable(Key.EXTRA_CAST_REQUEST);
            mVideoView.setVideoURI(Uri.parse(mediaRequest.videoURL));
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mp.start();
                }
            });
            if (mRendererService != null) {
                mRendererService.getAvTransportLastChange()
                        .setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.NO_MEDIA_PRESENT));
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        if (mRendererService != null) {
            mRendererService.unregisterControlBridge(mCastControlImp);
        }
        unbindService(mServiceConnection);
        super.onDestroy();
        mActivityDestroy = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mRendererService.getAudioControlLastChange()
                    .setEventedValue(INSTANCE_ID, new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, volume)));
        }
        return handled;
    }

    @Override
    public boolean isActivityDestroyed() {
        return mActivityDestroy;
    }
}
