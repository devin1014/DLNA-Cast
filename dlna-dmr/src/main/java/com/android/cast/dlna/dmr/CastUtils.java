package com.android.cast.dlna.dmr;

import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;

public class CastUtils {
    private CastUtils() {
    }

    public static TransportInfo getTransportInfo(int state) {
        //TODO
        // switch (state)
        // {
        //     case MediaControl.STATE_PREPARING:
        //
        //         return new TransportInfo(TransportState.TRANSITIONING);
        //
        //     case MediaControl.STATE_PREPARED:
        //     case MediaControl.STATE_PLAYING:
        //
        //         return new TransportInfo(TransportState.PLAYING);
        //
        //     case MediaControl.STATE_PAUSED:
        //
        //         return new TransportInfo(TransportState.PAUSED_PLAYBACK);
        //
        //     case MediaControl.STATE_COMPLETED:
        //     case MediaControl.STATE_ERROR:
        //
        //         return new TransportInfo(TransportState.STOPPED);
        // }

        return new TransportInfo(TransportState.STOPPED);
    }
}
