package com.android.cast.dlna;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.cast.dlna.controller.ICastControl;

import org.fourthline.cling.model.types.DeviceType;

/**
 *
 */
public interface IDLNACast extends ICastControl {

    void bindCastService(@NonNull Context context);

    void unbindCastService(@NonNull Context context);

    void search(DeviceType type, int maxSeconds);

    void clear();
}
