package com.neulion.android.upnpcast.util;

import android.util.Log;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-02
 * Time: 14:19
 */
public interface ILogger
{
    void d(String msg);

    void i(String msg);

    void w(String msg);

    void e(String msg);

    //boolean enable();

    class DefaultLoggerImpl implements ILogger
    {
        private static final String PREFIX = "4Droid_";
        private final String TAG;

        private final boolean DEBUG;

        public DefaultLoggerImpl(String tag)
        {
            this(tag, true);//FIXME
        }

        public DefaultLoggerImpl(String tag, boolean log)
        {
            TAG = PREFIX + tag;
            DEBUG = log;
        }

        @Override
        public void d(String msg)
        {
            if (DEBUG)
            {
                Log.d(TAG, msg);
            }
        }

        @Override
        public void i(String msg)
        {
            if (DEBUG)
            {
                Log.i(TAG, msg);
            }
        }

        @Override
        public void w(String msg)
        {
            if (DEBUG)
            {
                Log.w(TAG, msg);
            }
        }

        @Override
        public void e(String msg)
        {
            if (DEBUG)
            {
                Log.e(TAG, msg);
            }
        }

        //        @Override
        //        public boolean enable()
        //        {
        //            return DEBUG;
        //        }
    }
}
