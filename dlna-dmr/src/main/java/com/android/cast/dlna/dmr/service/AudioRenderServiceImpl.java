package com.android.cast.dlna.dmr.service;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;

public class AudioRenderServiceImpl extends AbstractAudioRenderingControl {
    private static final Channel[] mMasterChannel = new Channel[]{Channel.Master};
    private final RenderControlManager renderControlManager;

    public AudioRenderServiceImpl(LastChange lastChange, RenderControlManager renderControlManager) {
        super(lastChange);
        this.renderControlManager = renderControlManager;
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) {
        renderControlManager.getAudioControl(instanceId).setMute(channelName, desiredMute);
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) {
        return renderControlManager.getAudioControl(instanceId).getMute(channelName);
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) {
        renderControlManager.getAudioControl(instanceId).setVolume(channelName, desiredVolume);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) {
        return renderControlManager.getAudioControl(instanceId).getVolume(channelName);
    }

    @Override
    protected Channel[] getCurrentChannels() {
        return mMasterChannel;
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return renderControlManager.getAudioControlCurrentInstanceIds();
    }
}
