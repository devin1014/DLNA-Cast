package com.liuwei.android.upnpcast.controller;

/**
 */
public class CastObject
{
    public final String url;

    public final String id;

    public final String name;

    private long position;

    private long duration;

    public CastObject(String url, String id, String name)
    {
        this.url = url;
        this.id = id;
        this.name = name;
    }

    public static CastObject newInstance(String url, String id, String name)
    {
        return new CastObject(url, id, name);
    }

    public long getPosition()
    {
        return position;
    }

    public CastObject setPosition(long position)
    {
        this.position = position;

        return this;
    }

    /**
     * @return the total time of video (ms)
     */
    public long getDuration()
    {
        return duration;
    }

    /**
     * @param duration the total time of video (ms)
     */
    public CastObject setDuration(long duration)
    {
        this.duration = duration;

        return this;
    }
}
