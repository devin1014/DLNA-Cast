package com.android.cast.dlna.dms;

import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;

import java.util.HashMap;


public class ContentTree {

    public final static String ROOT_ID = "0";
    public final static String VIDEO_ID = "1";
    public final static String AUDIO_ID = "2";
    public final static String IMAGE_ID = "3";
    public final static String IMAGE_FOLD_ID = "4";
    public final static String VIDEO_PREFIX = "video-item-";
    public final static String AUDIO_PREFIX = "audio-item-";
    public final static String IMAGE_PREFIX = "image-item-";

    private static HashMap<String, ContentNode> contentMap = new HashMap<>();

    private static ContentNode rootNode = createRootNode();

    public ContentTree() {
    }

    ;

    protected static ContentNode createRootNode() {
        // create root container
        Container root = new Container();
        root.setId(ROOT_ID);
        root.setParentID("-1");
        root.setTitle("GNaP MediaServer root directory");
        root.setCreator("GNaP Media Server");
        root.setRestricted(true);
        root.setSearchable(true);
        root.setWriteStatus(WriteStatus.NOT_WRITABLE);
        root.setChildCount(0);
        ContentNode rootNode = new ContentNode(ROOT_ID, root);
        contentMap.put(ROOT_ID, rootNode);
        return rootNode;
    }

    public static ContentNode getRootNode() {
        return rootNode;
    }

    public static ContentNode getNode(String id) {
        if (contentMap.containsKey(id)) {
            return contentMap.get(id);
        }
        return null;
    }

    public static boolean hasNode(String id) {
        return contentMap.containsKey(id);
    }

    public static void addNode(String ID, ContentNode Node) {
        contentMap.put(ID, Node);
    }
}
