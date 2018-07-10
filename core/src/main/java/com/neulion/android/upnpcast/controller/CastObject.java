package com.neulion.android.upnpcast.controller;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 14:23
 */
public class CastObject
{
    public final String url;

    public final String id;

    public final String name;

    public final int duration;

    /**
     * @param duration the total time of video (ms)
     */
    public CastObject(String url, String id, String name, int duration)
    {
        this.url = url;
        this.id = id;
        this.name = name;
        this.duration = duration;
    }

    public static CastObject newInstance(String url, String id, String name, int duration)
    {
        return new CastObject(url, id, name, duration);
    }
}
