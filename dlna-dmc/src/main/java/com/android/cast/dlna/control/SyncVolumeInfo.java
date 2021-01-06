package com.android.cast.dlna.control;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;

final class SyncVolumeInfo extends SyncInfoRunnable<Integer> {

    public SyncVolumeInfo(@NonNull ControlPoint controlPoint,
                          @NonNull Service<?, ?> service,
                          @Nullable ICastInterface.ICastInfoListener<Integer> listener) {
        super(controlPoint, service, listener);
    }

    @Override
    protected long getSyncDuration() {
        return 3000L;
    }

    @Override
    protected String getActionName() {
        return "GetVolume";
    }

    @Override
    protected ActionCallback getAction() {
        return new GetVolume(getService()) {
            @Override
            public void received(ActionInvocation actionInvocation, int currentVolume) {
                setResult(currentVolume);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                setError(defaultMsg);
            }
        };
    }

}