package com.neulion.android.demo.render.utils;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

public class Utils
{
    public static long getThreadId()
    {
        Thread t = Thread.currentThread();
        return t.getId();
    }

    public static String getThreadSignature()
    {
        Thread t = Thread.currentThread();
        long l = t.getId();
        String name = t.getName();
        long p = t.getPriority();
        String gname = t.getThreadGroup().getName();
        return (name + ":(id)" + l + ":(priority)" + p + ":(group)" + gname);
    }

    public static void logThreadSignature(String tag)
    {
        Log.d(tag, getThreadSignature());
    }

    public static void sleepForInSecs(int secs)
    {
        try
        {
            Thread.sleep(secs * 1000);
        }
        catch (InterruptedException x)
        {
            throw new RuntimeException("interrupted", x);
        }
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size = 1024;
        try
        {
            byte[] bytes = new byte[buffer_size];
            for (; ; )
            {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                {
                    break;
                }
                os.write(bytes, 0, count);
            }
        }
        catch (Exception ex)
        {
        }
    }
}
