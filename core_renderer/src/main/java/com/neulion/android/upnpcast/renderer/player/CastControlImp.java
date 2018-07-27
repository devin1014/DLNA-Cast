package com.neulion.android.upnpcast.renderer.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;

import com.neulion.android.upnpcast.renderer.NLUpnpRendererService;
import com.neulion.android.upnpcast.renderer.localservice.IRendererInterface.IAVTransport;
import com.neulion.android.upnpcast.renderer.utils.ILogger;
import com.neulion.android.upnpcast.renderer.utils.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.MediaControl;
import com.neulion.media.control.MediaControl.OnPositionUpdateListener;
import com.neulion.media.control.MediaControl.SimpleCallback;
import com.neulion.media.control.MediaRequest;
import com.neulion.media.control.impl.CommonVideoController;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.AVTransportURI;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.CurrentTrackURI;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-26
 * Time: 18:30
 */
class CastControlImp implements ICastControl
{
    private MediaControl mMediaControl;

    private NLUpnpRendererService mRendererService;

    private LastChange mAvTransportLastChange;

    private LastChange mAudioControlLastChange;

    private Activity mActivity;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private AudioManager mAudioManager;

    CastControlImp(Activity activity, NLUpnpRendererService service, CommonVideoController controller)
    {
        mActivity = activity;

        mRendererService = service;

        mMediaControl = controller;

        mAvTransportLastChange = service.getAvTransportLastChange();

        mAudioControlLastChange = service.getAudioControlLastChange();

        mAudioManager = (AudioManager) activity.getApplication().getSystemService(Context.AUDIO_SERVICE);

        MediaCallback callback = new MediaCallback();

        controller.setOnPreparedListener(callback);

        controller.addOnPositionUpdateListener(callback);

        controller.setOnCompletionListener(callback);

        controller.setOnErrorListener(callback);

        controller.setCallback(callback);
    }

    @Override
    public void play()
    {
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mMediaControl != null)
                {
                    mMediaControl.resumeMedia();
                }
            }
        });
    }

    @Override
    public void pause()
    {
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mMediaControl != null)
                {
                    mMediaControl.pauseMedia();
                }
            }
        });
    }

    @Override
    public void seek(final long position)
    {
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mMediaControl != null)
                {
                    mMediaControl.seekTo(position);
                }
            }
        });
    }

    @Override
    public void stop()
    {
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mMediaControl != null)
                {
                    mMediaControl.releaseMedia();
                }

                mActivity.finish();
            }
        });
    }

    @Override
    public void setVolume(int volume)
    {
        final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume * maxVolume / 100, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);

        //RenderingControlVariable.Mute mute = (lastVolume == 0 || volume == 0) ? new RenderingControlVariable.Mute(new ChannelMute(Channel.Master, volume == 0)) : null;

        mAudioControlLastChange.setEventedValue(INSTANCE_ID, new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, volume)));
    }

    private final UnsignedIntegerFourBytes INSTANCE_ID = new UnsignedIntegerFourBytes(0);

    private class MediaCallback extends SimpleCallback implements MediaControl.OnPreparedListener,

            MediaControl.OnCompletionListener, MediaControl.OnErrorListener, OnPositionUpdateListener
    {
        private URI mURI;

        private ILogger mILogger = new DefaultLoggerImpl(this);

        @Override
        public void onOpen(MediaRequest request)
        {
            super.onOpen(request);
        }

        //        private synchronized void transportStateChanged(TransportState newState)
        //        {
        //            TransportState oldState = currentTransportInfo.getCurrentTransportState();
        //
        //            mLogger.d(String.format("transportStateChanged:[%s]->[%s]", oldState, newState));
        //
        //            currentTransportInfo = new TransportInfo(newState);
        //
        //            mAvTransportLastChange.setEventedValue(getInstanceId(), new AVTransportVariable.TransportState(newState),
        //
        //                    new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions()));
        //        }

        @Override
        public void onPreparing(MediaRequest request)
        {
            mILogger.d("onPreparing: " + request.getDataSource());

            try
            {
                mURI = new URI(request.getDataSource());
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onPrepared()
        {
            mILogger.d("onPrepared");

            if (mAvTransportLastChange != null)
            {
                mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportURI(mURI), new CurrentTrackURI(mURI));
            }
        }

        @Override
        public void onResume(boolean fromPause)
        {
            mILogger.d("onResume: " + fromPause);

            if (mAvTransportLastChange != null)
            {
                mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.PLAYING));
            }
        }

        @Override
        public void onPause(boolean fromResume)
        {
            mILogger.d("onPause: " + fromResume);

            if (mAvTransportLastChange != null)
            {
                mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.PAUSED_PLAYBACK));
            }
        }

        @Override
        public void onCompletion()
        {
            mILogger.d("onCompletion");

            if (mAvTransportLastChange != null)
            {
                mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.STOPPED));
            }

            mActivity.finish();
        }

        @Override
        public void onError(CharSequence errorMessage)
        {
            mILogger.d("onError: " + errorMessage);

            if (mAvTransportLastChange != null)
            {
                mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.STOPPED));
            }

            mActivity.finish();
        }

        @Override
        public void onRelease(boolean openingMedia)
        {
            mILogger.d("onRelease: " + openingMedia);

            if (!openingMedia)
            {
                if (mAvTransportLastChange != null)
                {
                    mAvTransportLastChange.setEventedValue(INSTANCE_ID, new AVTransportVariable.TransportState(TransportState.STOPPED));
                }
            }
        }

        @Override
        public void onPositionUpdate(long currentPosition)
        {
            for (UnsignedIntegerFourBytes id : mRendererService.getAVTransportControls().keySet())
            {
                IAVTransport avTransport = mRendererService.getAVTransportControls().get(id);

                avTransport.setCurrentPosition(currentPosition);

                avTransport.setDuration(mMediaControl.getDuration());
            }
        }
    }
}
