package com.neulion.android.upnpcast.controller;

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

    void onVolume(int volume);

    void onBrightness(int brightness);

    void onUpdatePositionInfo(PositionInfo positionInfo);
}
