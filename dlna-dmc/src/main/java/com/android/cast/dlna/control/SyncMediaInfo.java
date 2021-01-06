package com.android.cast.dlna.control;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.model.MediaInfo;

final class SyncMediaInfo extends SyncInfoRunnable<MediaInfo> {

    public SyncMediaInfo(@NonNull ControlPoint controlPoint,
                         @NonNull Service<?, ?> service,
                         @Nullable ICastInterface.ICastInfoListener<MediaInfo> listener) {
        super(controlPoint, service, listener);
    }

    @Override
    protected long getSyncDuration() {
        return 3000L;
    }

    @Override
    protected String getActionName() {
        return "GetMediaInfo";
    }

    @Override
    protected ActionCallback getAction() {
        return new GetMediaInfo(getService()) {
            @Override
            public void received(ActionInvocation invocation, MediaInfo mediaInfo) {
                setResult(mediaInfo);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                setError(defaultMsg);
            }
        };
    }

}