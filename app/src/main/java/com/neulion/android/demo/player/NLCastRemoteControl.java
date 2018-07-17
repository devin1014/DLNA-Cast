package com.neulion.android.demo.player;

import android.content.Context;

import com.neulion.android.upnpcast.Constants.Key;
import com.neulion.android.upnpcast.NLUpnpCastManager;
import com.neulion.android.upnpcast.controller.CastObject;
import com.neulion.android.upnpcast.util.CastUtils;
import com.neulion.android.upnpcast.util.ILogger;
import com.neulion.android.upnpcast.util.ILogger.DefaultLoggerImpl;
import com.neulion.media.control.MediaConnection.AbstractRemoteControl;
import com.neulion.media.control.MediaRequest;


public class NLCastRemoteControl extends AbstractRemoteControl
{
    private ILogger mLogger = new DefaultLoggerImpl(getClass().getSimpleName());
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
        NLUpnpCastManager.getInstance().cast(mCastObject);
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

        mCastManager.stop();

        onStatusUpdated();
    }

    @Override
    public void release()
    {
        mLogger.d("release");

        mCastManager.stop();

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
        return 0;
    }

    @Override
    public long getCurrentPosition()
    {
        return 0;
    }
}