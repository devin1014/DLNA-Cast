package com.android.cast.dlna.core;

import org.fourthline.cling.support.model.DIDLObject;

public enum ContentType {
    ALL("all", new DIDLObject.Class("object.container.all")),
    IMAGE("image", new DIDLObject.Class("object.item.imageItem")),
    AUDIO("audio", new DIDLObject.Class("object.container.audio")),
    VIDEO("video", new DIDLObject.Class("object.container.video"));

    public final String id;
    public final DIDLObject.Class clazz;

    ContentType(String name, DIDLObject.Class clazz) {
        this.id = name;
        this.clazz = clazz;
    }

    public boolean match(String id) {
        return this.id.equals(id);
    }
}
