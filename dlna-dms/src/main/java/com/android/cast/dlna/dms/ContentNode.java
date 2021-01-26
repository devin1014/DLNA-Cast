package com.android.cast.dlna.dms;

import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentNode {
    private final Container container;
    private final Item item;
    private final String id;
    private final String fullPath;
    private final boolean isItem;

    public ContentNode(String id, Container container) {
        this.id = id;
        this.container = container;
        this.item = null;
        this.fullPath = null;
        this.isItem = false;
    }

    @SuppressWarnings("unused")
    public ContentNode(String id, Item item, String fullPath) {
        this.id = id;
        this.item = item;
        this.fullPath = fullPath;
        this.container = null;
        this.isItem = true;
    }

    public String getId() {
        return id;
    }

    public Container getContainer() {
        return container;
    }

    public Item getItem() {
        return item;
    }

    public String getFullPath() {
        return isItem && fullPath != null ? fullPath : null;
    }

    public boolean isItem() {
        return isItem;
    }
}
