package com.android.cast.dlna.dmr.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RenderControlManager {

    private UnsignedIntegerFourBytes[] avControlUnsignedIntegerFourBytes = null;
    private final Map<UnsignedIntegerFourBytes, IRendererInterface.IAVTransportControl> avControlMap = new LinkedHashMap<>();
    private UnsignedIntegerFourBytes[] audioControlUnsignedIntegerFourBytes = null;
    private final Map<UnsignedIntegerFourBytes, IRendererInterface.IAudioControl> audioControlMap = new LinkedHashMap<>();

    public void addControl(@NonNull IRendererInterface.IControl control) {
        if (control instanceof IRendererInterface.IAVTransportControl) {
            avControlMap.put(control.getInstanceId(), (IRendererInterface.IAVTransportControl) control);
            avControlUnsignedIntegerFourBytes = null;
        } else if (control instanceof IRendererInterface.IAudioControl) {
            audioControlMap.put(control.getInstanceId(), (IRendererInterface.IAudioControl) control);
            audioControlUnsignedIntegerFourBytes = null;
        }
    }

    @Nullable
    public IRendererInterface.IAVTransportControl getAvTransportControl(UnsignedIntegerFourBytes instanceId) {
        return avControlMap.get(instanceId);
    }

    public UnsignedIntegerFourBytes[] getAvTransportCurrentInstanceIds() {
        if (avControlUnsignedIntegerFourBytes == null) {
            avControlUnsignedIntegerFourBytes = new UnsignedIntegerFourBytes[avControlMap.size()];
            avControlUnsignedIntegerFourBytes = avControlMap.keySet().toArray(new UnsignedIntegerFourBytes[0]);
        }
        return avControlUnsignedIntegerFourBytes;
    }

    @Nullable
    public IRendererInterface.IAudioControl getAudioControl(UnsignedIntegerFourBytes instanceId) {
        return audioControlMap.get(instanceId);
    }

    public UnsignedIntegerFourBytes[] getAudioControlCurrentInstanceIds() {
        if (audioControlUnsignedIntegerFourBytes == null) {
            audioControlUnsignedIntegerFourBytes = new UnsignedIntegerFourBytes[avControlMap.size()];
            audioControlUnsignedIntegerFourBytes = audioControlMap.keySet().toArray(new UnsignedIntegerFourBytes[0]);
        }
        return audioControlUnsignedIntegerFourBytes;
    }
}
