package com.android.cast.dlna.dms

import android.content.Context
import com.android.cast.dlna.core.ContentType.*
import com.android.cast.dlna.dms.IMediaContentDao.MediaContentDao
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode.CANNOT_PROCESS
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.Item

internal interface IContentFactory {
    @Throws(ContentDirectoryException::class)
    fun getBrowseResult(objectID: String): BrowseResult

    // ------------------------------------------------------------
    // ---- Implement
    // ------------------------------------------------------------
    class ContentFactoryImpl(
        context: Context,
        baseUrl: String
    ) : IContentFactory {

        @Throws(ContentDirectoryException::class)
        override fun getBrowseResult(objectID: String): BrowseResult {
            val container = getContent(objectID)
            val didlContent = DIDLContent()
            for (c in container.containers) didlContent.addContainer(c)
            for (item in container.items) didlContent.addItem(item)
            val count = container.childCount
            val result: String? = try {
                DIDLParser().generate(didlContent, true)
            } catch (e: Exception) {
                throw ContentDirectoryException(CANNOT_PROCESS, e.toString())
            }
            return BrowseResult(result, count.toLong(), count.toLong())
        }

        private fun getContent(containerId: String): Container {
            return if (ALL.match(containerId)) {
                val result = Container()
                // 定义音频资源
                val audioContainer = audioContents
                audioContainer.parentID = ALL.id
                audioContainer.id = AUDIO.id
                audioContainer.clazz = AUDIO.clazz
                audioContainer.title = AUDIO.name
                result.addContainer(audioContainer)
                // 定义图片资源
                val imageContainer = imageContents
                imageContainer.parentID = ALL.id
                imageContainer.id = IMAGE.id
                imageContainer.clazz = IMAGE.clazz
                imageContainer.title = IMAGE.name
                result.addContainer(imageContainer)
                // 定义视频资源
                val videoContainer = videoContents
                videoContainer.parentID = ALL.id
                videoContainer.id = VIDEO.id
                videoContainer.clazz = VIDEO.clazz
                videoContainer.title = VIDEO.name
                result.addContainer(videoContainer)
                // 3个子节点
                result.childCount = 3
                result
            } else if (IMAGE.match(containerId)) {
                imageContents
            } else if (AUDIO.match(containerId)) {
                audioContents
            } else if (VIDEO.match(containerId)) {
                videoContents
            } else {
                throw IllegalArgumentException("can not parse containerId$containerId")
            }
        }

        private val videoContents: Container = getContents(MediaContentDao(baseUrl).getVideoItems(context))
        private val audioContents: Container = getContents(MediaContentDao(baseUrl).getAudioItems(context))
        private val imageContents: Container = getContents(MediaContentDao(baseUrl).getImageItems(context))

        private fun getContents(items: List<Item?>): Container {
            val result = Container()
            for (item in items) result.addItem(item)
            result.childCount = items.size
            return result
        }
    }
}