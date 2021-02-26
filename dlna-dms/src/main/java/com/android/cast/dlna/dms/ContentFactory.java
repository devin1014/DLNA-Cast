package com.android.cast.dlna.dms;

import android.content.Context;

import androidx.annotation.Nullable;

import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.model.BrowseResult;

public class ContentFactory {

    private static class Holder {
        private static final ContentFactory sInstance = new ContentFactory();
    }

    public static ContentFactory getInstance() {
        return Holder.sInstance;
    }

    private ContentFactory() {
    }

    private IContentFactory mContentFactory;

    public void setServerUrl(Context context, String url) {
        mContentFactory = new IContentFactory.ContentFactoryImpl(context, url);
    }

    @Nullable
    public BrowseResult getContent(String objectID) throws ContentDirectoryException {
        if (mContentFactory != null) {
            return mContentFactory.getBrowseResult(objectID);
        }
        return null;
    }
}
