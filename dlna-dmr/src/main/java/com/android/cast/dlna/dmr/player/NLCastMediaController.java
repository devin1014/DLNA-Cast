package com.android.cast.dlna.dmr.player;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.dmr.DLNARendererService;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

/**
 *
 */
class NLCastMediaController implements ICastMediaControl {
    private final UnsignedIntegerFourBytes INSTANCE_ID = new UnsignedIntegerFourBytes(0);
    // private MediaControl mMediaControl;
    private DLNARendererService mRendererService;
    private LastChange mAvTransportLastChange;
    private LastChange mAudioControlLastChange;
    private Activity mActivity;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private AudioManager mAudioManager;
    private Object mMediaBinder;

    NLCastMediaController(@NonNull Fragment fragment, DLNARendererService service) {
        this(fragment.getActivity(), service);

        mMediaBinder = fragment;
    }

    NLCastMediaController(Activity activity, DLNARendererService service) {
        // mActivity = activity;
        //
        // mRendererService = service;
        //
        // mMediaControl = controller;
        //
        // mMediaBinder = activity;
        //
        // mAvTransportLastChange = service.getAvTransportLastChange();
        //
        // mAudioControlLastChange = service.getAudioControlLastChange();
        //
        // mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        //
        // MediaCallback callback = new MediaCallback();
        //
        // controller.setOnPreparedListener(callback);
        //
        // controller.addOnPositionUpdateListener(callback);
        //
        // controller.setOnCompletionListener(callback);
        //
        // controller.setOnErrorListener(callback);
        //
        // if (controller instanceof CommonVideoController) {
        //     ((CommonVideoController) controller).setCallback(callback);
        // } else if (controller instanceof CommonVideoView) {
        //     ((CommonVideoView) controller).setCallback(callback);
        // }
    }

    @Override
    public void play() {
        // mHandler.post(new Runnable() {
        //     @Override
        //     public void run() {
        //         if (mMediaControl != null) {
        //             mMediaControl.resumeMedia();
        //         }
        //     }
        // });
    }

    @Override
    public void pause() {
        // mHandler.post(new Runnable() {
        //     @Override
        //     public void run() {
        //         if (mMediaControl != null) {
        //             mMediaControl.pauseMedia();
        //         }
        //     }
        // });
    }

    @Override
    public void seek(final long position) {
        // mHandler.post(new Runnable() {
        //     @Override
        //     public void run() {
        //         if (mMediaControl != null) {
        //             mMediaControl.seekTo(position);
        //         }
        //     }
        // });
    }

    @Override
    public void stop() {
        // mHandler.post(new Runnable() {
        //     @Override
        //     public void run() {
        //         if (mMediaControl != null) {
        //             mMediaControl.releaseMedia();
        //         }
        //
        //         mActivity.finish();
        //     }
        // });
    }

    @Override
    public void setVolume(int volume) {
        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume * maxVolume / 100, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);

        //RenderingControlVariable.Mute mute = (lastVolume == 0 || volume == 0) ? new RenderingControlVariable.Mute(new ChannelMute(Channel.Master, volume == 0)) : null;

        mAudioControlLastChange.setEventedValue(INSTANCE_ID, new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, volume)));
    }

    // private class MediaCallback extends SimpleCallback implements MediaControl.OnPreparedListener,
    //         MediaControl.OnCompletionListener,
    //         MediaControl.OnErrorListener,
    //         MediaControl.OnPositionUpdateListener {
    //     private URI mURI;
    //
    //     private ILogger mILogger = new DefaultLoggerImpl(this);
    //
    //     @Override
    //     public void onOpen(MediaRequest request) {
    //         mILogger.i("onOpen: " + request.getDataSource());
    //     }
    //
    //     @Override
    //     public void onPreparing(MediaRequest request) {
    //         mILogger.i("onPreparing: " + request.getDataSource());
    //
    //         try {
    //             mURI = new URI(request.getDataSource());
    //         } catch (URISyntaxException e) {
    //             e.printStackTrace();
    //         }
    //
    //         updateMediaState(MediaControl.STATE_PREPARING);
    //     }
    //
    //     @Override
    //     public void onPrepared() {
    //         mILogger.i("onPrepared");
    //
    //         if (mMediaBinder instanceof OnPreparedListener) {
    //             ((OnPreparedListener) mMediaBinder).onPrepared();
    //         }
    //
    //         updateMediaState(MediaControl.STATE_PREPARED);
    //
    //         if (mAvTransportLastChange != null) {
    //             mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportURI(mURI), new CurrentTrackURI(mURI));
    //         }
    //     }
    //
    //     @Override
    //     public void onResume(boolean fromPause) {
    //         mILogger.i("onResume: " + fromPause);
    //
    //         updateMediaState(MediaControl.STATE_PLAYING);
    //
    //         if (mAvTransportLastChange != null) {
    //             mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.PLAYING));
    //         }
    //     }
    //
    //     @Override
    //     public void onPause(boolean fromResume) {
    //         mILogger.i("onPause: " + fromResume);
    //
    //         updateMediaState(MediaControl.STATE_PAUSED);
    //
    //         if (mAvTransportLastChange != null) {
    //             mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.PAUSED_PLAYBACK));
    //         }
    //     }
    //
    //     @Override
    //     public void onCompletion() {
    //         mILogger.i("onCompletion");
    //
    //         if (mMediaBinder instanceof OnCompletionListener) {
    //             ((OnCompletionListener) mMediaBinder).onCompletion();
    //         }
    //
    //         updateMediaState(MediaControl.STATE_COMPLETED);
    //
    //         if (mAvTransportLastChange != null) {
    //             mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.STOPPED));
    //         }
    //
    //         mActivity.finish();
    //     }
    //
    //     @Override
    //     public void onError(CharSequence errorMessage) {
    //         mILogger.e("onError: " + errorMessage);
    //
    //         if (mMediaBinder instanceof OnErrorListener) {
    //             ((OnErrorListener) mMediaBinder).onError(errorMessage);
    //         }
    //
    //         updateMediaState(MediaControl.STATE_ERROR);
    //
    //         if (mAvTransportLastChange != null) {
    //             mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.STOPPED));
    //         }
    //
    //         mActivity.finish();
    //     }
    //
    //     @Override
    //     public void onRelease(boolean openingMedia) {
    //         mILogger.i("onRelease: " + openingMedia);
    //
    //         if (!openingMedia) {
    //             updateMediaState(MediaControl.STATE_IDLE);
    //
    //             if (mAvTransportLastChange != null) {
    //                 mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.STOPPED));
    //             }
    //         }
    //     }
    //
    //     @Override
    //     public void onPositionUpdate(long currentPosition) {
    //         if (mMediaBinder instanceof OnPositionUpdateListener) {
    //             ((OnPositionUpdateListener) mMediaBinder).onPositionUpdate(currentPosition);
    //         }
    //
    //         for (UnsignedIntegerFourBytes id : mRendererService.getAVTransportControls().keySet()) {
    //             IAVTransport avTransport = mRendererService.getAVTransportControls().get(id);
    //
    //             avTransport.updateMediaCurrentPosition(currentPosition);
    //
    //             avTransport.updateMediaDuration(mMediaControl.getDuration());
    //         }
    //     }
    //
    //     private void updateMediaState(int state) {
    //         for (UnsignedIntegerFourBytes id : mRendererService.getAVTransportControls().keySet()) {
    //             IAVTransport avTransport = mRendererService.getAVTransportControls().get(id);
    //
    //             avTransport.updateMediaState(state);
    //         }
    //     }
    // }
}
