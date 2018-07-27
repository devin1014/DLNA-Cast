package com.neulion.android.upnpcast.renderer.localservice;

import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-25
 * Time: 15:33
 */
public interface IAudioControl
{
    void setMute(String channelName, boolean desiredMute);

    boolean getMute(String channelName);

    void setVolume(String channelName, UnsignedIntegerTwoBytes desiredVolume);

    UnsignedIntegerTwoBytes getVolume(String channelName);
}
