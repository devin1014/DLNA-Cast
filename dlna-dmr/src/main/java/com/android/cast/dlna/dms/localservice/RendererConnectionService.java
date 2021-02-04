package com.android.cast.dlna.dms.localservice;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;

public class RendererConnectionService extends ConnectionManagerService {
    public RendererConnectionService() {
        try {
            sinkProtocolInfo.add(new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/mpeg", "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"));
            sinkProtocolInfo.add(new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
}
