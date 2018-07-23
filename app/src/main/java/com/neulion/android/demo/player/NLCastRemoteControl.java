package com.neulion.android.demo.player;

import android.content.Context;

import com.neulion.android.upnpcast.Constants.Key;
import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.controller.CastObject;
import com.neulion.android.upnpcast.controller.DefaultCastEventListener;
import com.neulion.android.upnpcast.util.CastUtils;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.MediaConnection.AbstractRemoteControl;
import com.neulion.media.control.MediaRequest;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;


public class NLCastRemoteControl extends AbstractRemoteControl
{
    private ILogger mLogger = new DefaultLoggerImpl(this);
    private NLUpnpCastManager mCastManager;
    private final CastObject mCastObject;

    public NLCastRemoteControl(Context context, MediaRequest request)
    {
        mCastManager = NLUpnpCastManager.getInstance();

        mCastObject = new CastObject(request.getDataSource(), (String) request.getParam(Key.ID), (String) request.getParam(Key.NAME));

        mCastObject.setPosition(CastUtils.parseTime((String) request.getParam(Key.POSITION)));

        mCastObject.setDuration(CastUtils.parseTime((String) request.getParam(Key.DURATION)));
    }

    @Override
    public void prepareAsync()
    {
        mLogger.d("prepareAsync");
        mCastManager.addCastEventListener(mCastEventListener);

        boolean casting = false;

        if (mCastManager.isConnected())
        {
            MediaInfo mediaInfo = mCastManager.getMedia();

            if (mediaInfo != null && mediaInfo.getCurrentURI().equals(mCastObject.url))
            {
                casting = true;
            }
        }

        if (!casting)
        {
            mCastManager.cast(mCastObject);
        }

        onPrepared();
    }

    @Override
    public void start()
    {
        mLogger.d("start");

        mCastManager.start();

        onStatusUpdated();
    }

    @Override
    public void pause()
    {
        mLogger.d("pause");

        mCastManager.pause();

        onStatusUpdated();
    }

    @Override
    public void stop()
    {
        mLogger.d("stop");

        //mCastManager.stop();

        onStatusUpdated();
    }

    @Override
    public void release()
    {
        mLogger.d("release");

        mCastManager.removeCastEventListener(mCastEventListener);

        //mCastManager.stop();

        onStatusUpdated();
    }

    @Override
    public void seekTo(long position)
    {
        mLogger.d(String.format("seekTo:[%s ms]", position));

        mCastManager.seekTo(position);

        onStatusUpdated();
    }

    @Override
    public void setVolume(float volumeLeft, float volumeRight)
    {
        mLogger.d(String.format("setVolume:[%s,%s]", volumeLeft, volumeRight));
        //mCastManager.setVolume();
        onStatusUpdated();
    }

    @Override
    public boolean isLive()
    {
        return false;
    }

    @Override
    public boolean isPlaying()
    {
        return mCastManager.getCastStatus() == NLUpnpCastManager.PLAY;
    }

    @Override
    public boolean isPaused()
    {
        return mCastManager.getCastStatus() == NLUpnpCastManager.PAUSE;
    }

    @Override
    public boolean isStopped()
    {
        return mCastManager.getCastStatus() == NLUpnpCastManager.STOP;
    }

    @Override
    public int getVideoWidth()
    {
        return 0;
    }

    @Override
    public int getVideoHeight()
    {
        return 0;
    }

    @Override
    public long getDuration()
    {
        PositionInfo positionInfo = mCastManager.getPosition();

        return CastUtils.getIntTime(positionInfo != null ? positionInfo.getTrackDuration() : null);
    }

    @Override
    public long getCurrentPosition()
    {
        PositionInfo positionInfo = mCastManager.getPosition();

        return CastUtils.getIntTime(positionInfo != null ? positionInfo.getRelTime() : null);
    }

    private DefaultCastEventListener mCastEventListener = new DefaultCastEventListener()
    {
        @Override
        public void onCast(CastObject castObject)
        {
            super.onCast(castObject);

            onStatusUpdated();
        }

        @Override
        public void onStart()
        {
            super.onStart();

            onStatusUpdated();
        }

        @Override
        public void onPause()
        {
            super.onPause();

            onStatusUpdated();
        }

        @Override
        public void onStop()
        {
            super.onStop();

            onStatusUpdated();
        }

        @Override
        public void onError(String errorMsg)
        {
            super.onError(errorMsg);

            onStatusUpdated();
        }
    };
}