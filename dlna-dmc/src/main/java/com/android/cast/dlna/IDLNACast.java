package com.android.cast.dlna;

import com.android.cast.dlna.controller.ICastControl;

import org.fourthline.cling.model.types.DeviceType;

/**
 *
 */
public interface IDLNACast extends ICastControl {
    int DEFAULT_MAX_SECONDS = 60;

    void search();

    void search(DeviceType type, int maxSeconds);

    void clear();
}
