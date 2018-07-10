package com.neulion.android.upnpcast.controller;

import com.neulion.android.upnpcast.device.CastDevice;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 14:22
 */
public interface ICastControlListener
{
    void onOpen(String url);

    void onStart();

    void onPause();

    void onStop();

    void onSeekTo(long position);

    void onError(String errorMsg);

    void onVolume(long volume);

    void onSyncMediaInfo(CastDevice castDevice, MediaInfo mediaInfo);

    void onMediaPositionInfo(PositionInfo positionInfo);
}
