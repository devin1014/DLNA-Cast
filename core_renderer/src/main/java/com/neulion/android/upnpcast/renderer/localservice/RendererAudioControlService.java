package com.neulion.android.upnpcast.renderer.localservice;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlErrorCode;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

import java.util.Map;

public class RendererAudioControlService extends AbstractAudioRenderingControl
{
    private Map<UnsignedIntegerFourBytes, IAudioControl> mRendererControls;

    private UnsignedIntegerFourBytes[] mUnsignedIntegerFourBytes;

    public RendererAudioControlService(LastChange lastChange, Map<UnsignedIntegerFourBytes, IAudioControl> audioControls)
    {
        super(lastChange);

        mRendererControls = audioControls;

        mUnsignedIntegerFourBytes = new UnsignedIntegerFourBytes[audioControls.size()];

        int i = 0;

        for (UnsignedIntegerFourBytes id : mRendererControls.keySet())
        {
            mUnsignedIntegerFourBytes[i] = id;

            i++;
        }
    }

    private IAudioControl getInstance(UnsignedIntegerFourBytes instanceId) throws RenderingControlException
    {
        IAudioControl player = mRendererControls.get(instanceId);

        if (player == null)
        {
            throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID);
        }

        return player;
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException
    {
        getInstance(instanceId).setMute(channelName, desiredMute);
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException
    {
        return getInstance(instanceId).getMute(channelName);
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException
    {
        getInstance(instanceId).setVolume(channelName, desiredVolume);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException
    {
        return getInstance(instanceId).getVolume(channelName);
    }

    @Override
    protected Channel[] getCurrentChannels()
    {
        return new Channel[]{Channel.Master};
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds()
    {
        return mUnsignedIntegerFourBytes;
    }
}
