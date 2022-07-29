package com.android.cast.dlna.dmr.service

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService
import org.fourthline.cling.support.model.Protocol.HTTP_GET
import org.fourthline.cling.support.model.ProtocolInfo
import java.lang.IllegalArgumentException

class ConnectionManagerServiceImpl : ConnectionManagerService() {
    init {
        try {
            sinkProtocolInfo.add(ProtocolInfo(HTTP_GET, ProtocolInfo.WILDCARD, "audio/mpeg", "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"))
            sinkProtocolInfo.add(ProtocolInfo(HTTP_GET, ProtocolInfo.WILDCARD, "video/mpeg", "DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"))
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
    }
}