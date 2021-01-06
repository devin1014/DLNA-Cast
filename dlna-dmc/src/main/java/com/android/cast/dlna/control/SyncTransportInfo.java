package com.android.cast.dlna.control;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.model.TransportInfo;

final class SyncTransportInfo extends SyncInfoRunnable<TransportInfo> {

    public SyncTransportInfo(@NonNull ControlPoint controlPoint,
                             @NonNull Service<?, ?> service,
                             @Nullable ICastInterface.ICastInfoListener<TransportInfo> listener) {
        super(controlPoint, service, listener);
    }

    @Override
    protected String getActionName() {
        return "GetTransportInfo";
    }

    @Override
    protected ActionCallback getAction() {
        return new GetTransportInfo(getService()) {
            @Override
            public void received(ActionInvocation invocation, TransportInfo transportInfo) {
                setResult(transportInfo);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                setError(defaultMsg);
            }
        };
    }

}
