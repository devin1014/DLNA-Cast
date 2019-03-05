package com.liuwei.android.upnpcast.controller;

import org.fourthline.cling.support.model.PositionInfo;

/**
 */
public interface ICastControlListener
{
    void onCast(CastObject castObject);

    void onStart();

    void onPause();

    void onStop();

    void onSeekTo(long position);

    void onError(String errorMsg);

    void onVolume(int volume);

    void onBrightness(int brightness);

    void onUpdatePositionInfo(PositionInfo positionInfo);
}
