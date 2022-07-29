package com.android.cast.dlna.dms

import android.content.Context
import com.android.cast.dlna.dms.IContentFactory.ContentFactoryImpl
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.model.BrowseResult

object ContentFactory {

    private var contentFactory: IContentFactory? = null

    fun setServerUrl(context: Context?, url: String?) {
        contentFactory = ContentFactoryImpl(context, url)
    }

    @Throws(ContentDirectoryException::class)
    fun getContent(objectID: String?): BrowseResult? = contentFactory?.getBrowseResult(objectID)
}