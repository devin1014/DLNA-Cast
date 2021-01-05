package com.android.cast.dlna.demo;

import org.fourthline.cling.model.meta.Device;

interface IDisplayDevice {
    void setCastDevice(Device<?, ?, ?> device);
}
