package com.android.cast.dlna.demo;

/**
 *
 */
public class Constants {
    static final String CAST_URL_IPHONE_SAMPLE = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8";
    static final String CAST_URL_HLS_BT_INNER = "http://mobile.neulion.net.cn/ftp/public/test/bt/playlist2.m3u8";
    static final String CAST_URL_HLS_CC_INNER = "http://mobile.neulion.net.cn/ftp/public/test/cc/playlist.m3u8";
    static final String CAST_URL_HLS_360_INNER = "http://mobile.neulion.net.cn/ftp/public/test/360/mobile_1440_1080_ipad.mp4.m3u8";
    static final String CAST_URL_MP4_INNER = "http://mobile.neulion.net.cn/ftp/public/test/mp4/PC_1600.MP4";

    private static final int _30_MIN = 30 * 60 * 1000;

    public static final String CAST_ID = "101";
    public static final String CAST_NAME = "castDemo";
    public static final String CAST_URL = CAST_URL_MP4_INNER;
    //TODO: check get cast video duration.
    public static final int CAST_VIDEO_DURATION = _30_MIN;
}
