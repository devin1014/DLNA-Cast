package com.neulion.android.demo.render.upnp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.neulion.android.demo.render.MediaRendererActivity;
import com.neulion.android.demo.render.MediaRendererService;
import com.neulion.android.demo.render.RendererApplication;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.net.URI;

public class DefMediaControl
{
    final private static String TAG = "DefMediaControl";

    private UnsignedIntegerFourBytes instanceId;
    private LastChange avTransportLastChange;
    private LastChange renderingControlLastChange;

    private volatile TransportInfo currentTransportInfo = new TransportInfo();
    private PositionInfo currentPositionInfo = new PositionInfo();
    private MediaInfo currentMediaInfo = new MediaInfo();
    private double storedVolume;

    private Context context;

    public DefMediaControl(Context c, UnsignedIntegerFourBytes instanceId, LastChange avTransportLastChange, LastChange renderingControlLastChange)
    {
        context = c;
        this.instanceId = instanceId;
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;
    }

    public UnsignedIntegerFourBytes getInstanceId()
    {
        return instanceId;
    }

    public LastChange getAvTransportLastChange()
    {
        return avTransportLastChange;
    }

    public LastChange getRenderingControlLastChange()
    {
        return renderingControlLastChange;
    }

    synchronized public TransportInfo getCurrentTransportInfo()
    {
        return currentTransportInfo;
    }

    synchronized public PositionInfo getCurrentPositionInfo()
    {
        currentPositionInfo = new PositionInfo(1, ModelUtil.toTimeString(MediaRendererActivity.tackTime() / 1000), currentMediaInfo.getCurrentURI(), ModelUtil.toTimeString(MediaRendererActivity
                .curentTime() / 1000), ModelUtil.toTimeString(MediaRendererActivity.curentTime() / 1000));

        return currentPositionInfo;
    }

    synchronized public MediaInfo getCurrentMediaInfo()
    {
        return currentMediaInfo;
    }

    synchronized public void setVolume(float leftVolume, float rightVolume)
    {
        MediaRendererActivity.setVolume(leftVolume);
    }

    synchronized public int getVolume()
    {
        return MediaRendererActivity.getVolume();
    }

    synchronized public void setVolume(double volume)
    {
        Log.d(TAG, "setVolume");
        storedVolume = getVolume();
        setVolume((float) volume, (float) volume);

        ChannelMute switchedMute = (storedVolume == 0 && volume > 0) || (storedVolume > 0 && volume == 0) ? new ChannelMute(Channel.Master, storedVolume > 0 && volume == 0) : null;

        getRenderingControlLastChange().setEventedValue(getInstanceId(), new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, (int) (volume))),
                switchedMute != null ? new RenderingControlVariable.Mute(switchedMute) : null);
    }

    synchronized public void setMute(boolean desiredMute)
    {
        if (desiredMute && getVolume() > 0)
        {
            setVolume(0);
        }
        else if (!desiredMute && getVolume() == 0)
        {
            setVolume(storedVolume);
        }
    }

    synchronized public TransportAction[] getCurrentTransportActions()
    {
        TransportState state = currentTransportInfo.getCurrentTransportState();
        TransportAction[] actions;

        switch (state)
        {
            case STOPPED:
                actions = new TransportAction[]{TransportAction.Play};
                break;
            case PLAYING:
                actions = new TransportAction[]{TransportAction.Stop, TransportAction.Pause, TransportAction.Seek};
                break;
            case PAUSED_PLAYBACK:
                actions = new TransportAction[]{TransportAction.Stop, TransportAction.Pause, TransportAction.Seek, TransportAction.Play};
                break;
            default:
                actions = null;
        }
        return actions;
    }

    synchronized public void transportStateChanged(TransportState newState)
    {
        TransportState currentTransportState = currentTransportInfo.getCurrentTransportState();
        Log.d(TAG, "Current state is: " + currentTransportState + ", changing to new state: " + newState);
        currentTransportInfo = new TransportInfo(newState);
        getAvTransportLastChange().setEventedValue(getInstanceId(), new AVTransportVariable.TransportState(newState), new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions()));
    }

    synchronized public void setURI(final URI uri)
    {
        Log.d(TAG, "setURI is called");
        RendererApplication.setUrl(uri.toString());
        try
        {

            if (!MediaRendererActivity.getStatusAct())
            {
                Intent intent = new Intent(context, MediaRendererActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            else
            {
                context.sendBroadcast(new Intent(MediaRendererService.ACTION_SETURI));
            }

        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception : " + e.getMessage());
        }

        currentMediaInfo = new MediaInfo(uri.toString(), "", getInstanceId(), ModelUtil.toTimeString(MediaRendererActivity.tackTime() / 1000), StorageMedium.NETWORK);
        currentPositionInfo = new PositionInfo(1, "", uri.toString());

        getAvTransportLastChange().setEventedValue(getInstanceId(), new AVTransportVariable.AVTransportURI(uri), new AVTransportVariable.CurrentTrackURI(uri));

        transportStateChanged(TransportState.STOPPED);
    }

    synchronized public void seekTo(final int msec)
    {
        try
        {
            MediaRendererActivity.seekTo(msec);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception : " + e.getMessage());
        }

        transportStateChanged(TransportState.PLAYING);
    }

    synchronized public void pause()
    {
        Log.d(TAG, "pause is called");

        try
        {
            MediaRendererActivity.pause();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception : " + e.getMessage());
        }

        transportStateChanged(TransportState.PAUSED_PLAYBACK);
    }

    synchronized public void play()
    {
        Log.d(TAG, "play is called");
        RendererApplication.setPlayMode(true);

        try
        {
            MediaRendererActivity.play();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception : " + e.getMessage());
        }

        transportStateChanged(TransportState.PLAYING);
    }

    synchronized public void stop()
    {
        Log.d(TAG, "stopPlayback is called");
        try
        {
            MediaRendererActivity.stop();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception : " + e.getMessage());
        }

        transportStateChanged(TransportState.STOPPED);
    }

    synchronized public void complete()
    {
        Log.d(TAG, "onCompletion");
        transportStateChanged(TransportState.STOPPED);
    }

}
