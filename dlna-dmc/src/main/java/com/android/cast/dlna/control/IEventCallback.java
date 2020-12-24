package com.android.cast.dlna.control;

import org.fourthline.cling.support.model.TransportInfo;

public interface IEventCallback {

    interface ITransportChangedCallback extends IEventCallback {
        void onTransportChanged(TransportInfo transportInfo);
    }

    interface IVolumeChangedCallback extends IEventCallback {
        void onVolumeChanged(int volume);
    }
}


