package com.android.cast.dlna.core

import org.fourthline.cling.support.model.DIDLObject.Class

enum class ContentType(val id: String, val clazz: Class) {
    ALL("all", Class("object.container.all")),
    IMAGE("image", Class("object.item.imageItem")),
    AUDIO("audio", Class("object.container.audio")),
    VIDEO("video", Class("object.container.video"));

    fun match(id: String): Boolean {
        return this.id == id
    }
}