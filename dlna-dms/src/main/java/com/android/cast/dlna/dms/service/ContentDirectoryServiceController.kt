package com.android.cast.dlna.dms.service

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore.Video
import com.android.cast.dlna.core.Logger
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.Item
import kotlin.math.max

class ContentDirectoryServiceController(context: Context) : ContentControl {
    companion object {
        private val columns = arrayOf(MediaColumns._ID, MediaColumns.TITLE, MediaColumns.DATA, MediaColumns.MIME_TYPE, MediaColumns.SIZE)
    }

    private val logger = Logger.create("ContentDirectoryService")
    private val applicationContext: Context = context.applicationContext
    override fun browse(objectID: String, browseFlag: BrowseFlag, filter: String?, firstResult: Long, maxResults: Long): BrowseResult {
        logger.i("browse: $objectID, $browseFlag, $filter, $firstResult, $maxResults")
        return get(objectID, filter, firstResult, maxResults)
    }

    override fun search(containerId: String, searchCriteria: String, filter: String?, firstResult: Long, maxResults: Long): BrowseResult {
        logger.i("search: $containerId, $searchCriteria, $filter, $firstResult, $maxResults")
        return get(containerId, filter, firstResult, maxResults)
    }

    private fun get(objectID: String, filter: String?, firstResult: Long, maxResults: Long): BrowseResult {
        var maxCount = maxResults
        val list = mutableListOf<Item>()
        val didlContent = DIDLContent()
        if (objectID.isBlank() || objectID == "*" || objectID.contains("video")) {
            list.addAll(getItems(applicationContext, Video.Media.EXTERNAL_CONTENT_URI, firstResult, maxCount))
            maxCount -= list.size
        }
        if (objectID.isBlank() || objectID == "*" || objectID.contains("audio")) {
            list.addAll(getItems(applicationContext, Audio.Media.EXTERNAL_CONTENT_URI, firstResult, maxCount))
            maxCount -= list.size
        }
        if (objectID.isBlank() || objectID == "*" || objectID.contains("image")) {
            list.addAll(getItems(applicationContext, Images.Media.EXTERNAL_CONTENT_URI, firstResult, maxCount))
            maxCount -= list.size
        }
        didlContent.items = list
        return try {
            val result = DIDLParser().generate(didlContent, false)
            BrowseResult(result, didlContent.items.size.toLong(), didlContent.items.size.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
            BrowseResult("", 0, 0) //TODO: check
        }
    }

    @SuppressLint("Range")
    private fun getItems(context: Context, uri: Uri, firstResult: Long, maxResults: Long): List<Item> {
        val first = max(firstResult, 0).toInt()
        val max = max(maxResults, 0)
        val items: MutableList<Item> = ArrayList()
        context.contentResolver.query(uri, columns, null, null, null)
            .use { cursor ->
                if (cursor == null) return items
                cursor.moveToPosition(first)
                while (cursor.moveToNext() && items.size < max) {
                    val id = cursor.getInt(cursor.getColumnIndex(columns[0])).toString()
                    val title = cursor.getString(cursor.getColumnIndex(columns[1]))
                    val data = cursor.getString(cursor.getColumnIndex(columns[2]))
                    val mimeType = cursor.getString(cursor.getColumnIndex(columns[3]))
                    val size = cursor.getLong(cursor.getColumnIndex(columns[4]))
                    items.add(ImageItem(id, "-1", title, "", Res(mimeType, size, "", null, data)))
                }
            }
        return items
    }
}