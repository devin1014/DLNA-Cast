package com.android.cast.dlna.util;

import android.util.Log;

import com.android.cast.dlna.Constants;


/**
 *
 */
public interface ILogger {
    String PREFIX_TAG = "DLNACast_";

    void v(String msg);

    void d(String msg);

    void i(String msg);

    void w(String msg);

    void e(String msg);

    class DefaultLoggerImpl implements ILogger {
        private final String TAG;
        private final boolean DEBUG;

        public DefaultLoggerImpl(Object object) {
            this(object.getClass().getSimpleName() + "@" + Integer.toHexString(object.hashCode()));
        }

        public DefaultLoggerImpl(String tag) {
            this(tag, Constants.DEBUG);
        }

        public DefaultLoggerImpl(String tag, boolean debug) {
            TAG = PREFIX_TAG + tag;
            DEBUG = debug;
        }

        @Override
        public void v(String msg) {
            if (DEBUG) {
                Log.v(TAG, msg);
            }
        }

        @Override
        public void d(String msg) {
            if (DEBUG) {
                Log.d(TAG, msg);
            }
        }

        @Override
        public void i(String msg) {
            if (DEBUG) {
                Log.i(TAG, msg);
            }
        }

        @Override
        public void w(String msg) {
            if (DEBUG) {
                Log.w(TAG, msg);
            }
        }

        @Override
        public void e(String msg) {
            if (DEBUG) {
                Log.e(TAG, msg);
            }
        }
    }
}
