package com.android.cast.dlna.control;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.model.PositionInfo;

final class SyncPositionInfo extends SyncInfoRunnable<PositionInfo> {

    public SyncPositionInfo(@NonNull ControlPoint controlPoint,
                            @NonNull Service<?, ?> service,
                            @Nullable ICastInterface.ICastInfoListener<PositionInfo> listener) {
        super(controlPoint, service, listener);
    }

    @Override
    protected String getActionName() {
        return "GetPositionInfo";
    }

    @Override
    protected ActionCallback getAction() {
        return new GetPositionInfo(getService()) {
            @Override
            public void received(ActionInvocation invocation, PositionInfo positionInfo) {
                setResult(positionInfo);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                setError(defaultMsg);
            }
        };
    }

}