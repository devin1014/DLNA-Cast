package com.neulion.android.demo.upnpcast;

import com.neulion.android.upnpcast.controller.CastObject;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-16
 * Time: 17:04
 */
public class Constants
{
    private static final String CAST_URL_LOCAL_TEST = "http://172.16.0.107:8506/clear/teststage/t594_hd_apptv.m3u8";
    private static final String CAST_URL_CNTV_TEST = "http://stdev.nlv2.com/live/cntv/2013/cctv5_ipad.m3u8?auth_key=3af822d1cb3011bcd2f3b6d9a11396df-1532406130-32-*.m3u8;*.ts";
    private static final String CAST_URL_IPHONE_SAMPLE = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
    private static final int _30_MIN = 30 * 60 * 1000;

    public static final String CAST_URL = CAST_URL_IPHONE_SAMPLE;
    private static final String CAST_ID = "101";
    private static final String CAST_NAME = "castDemo";
    public static final int CAST_VIDEO_DURATION = 0; //TODO: check get cast video duration.

    public static final CastObject CAST_OBJECT;

    static
    {
        CAST_OBJECT = CastObject.newInstance(CAST_URL, CAST_ID, CAST_NAME);
    }
}
