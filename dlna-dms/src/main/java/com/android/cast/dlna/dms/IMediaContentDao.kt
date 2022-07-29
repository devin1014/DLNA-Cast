package com.android.cast.dlna.dms

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore.*
import android.provider.MediaStore.Images.Media
import com.android.cast.dlna.core.ContentType.*
import org.fourthline.cling.support.model.PersonWithRole
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.Item
import org.fourthline.cling.support.model.item.Movie
import org.fourthline.cling.support.model.item.MusicTrack

// ------------------------------------------------------------
// ---- MediaContent
// ------------------------------------------------------------
interface IMediaContentDao {

    companion object {
        private val CONTENT_IMAGE_COLUMNS = arrayOf(
            MediaColumns._ID,
            MediaColumns.TITLE,
            MediaColumns.DATA,
            MediaColumns.MIME_TYPE,
            MediaColumns.SIZE
        )
        private val CONTENT_AUDIO_COLUMNS = arrayOf(
            MediaColumns._ID,
            MediaColumns.TITLE,
            MediaColumns.ARTIST,
            MediaColumns.DATA,
            MediaColumns.MIME_TYPE,
            MediaColumns.SIZE,
            MediaColumns.ALBUM
        )
        private val CONTENT_VIDEO_COLUMNS = arrayOf(
            MediaColumns._ID,
            MediaColumns.TITLE,
            MediaColumns.ARTIST,
            MediaColumns.DATA,
            MediaColumns.MIME_TYPE,
            MediaColumns.SIZE,
            MediaColumns.RESOLUTION
        )
    }

    fun getImageItems(context: Context): List<Item?>
    fun getAudioItems(context: Context): List<Item?>
    fun getVideoItems(context: Context): List<Item?>

    // -------------------------------------------------------
    // ---- Implement
    // -------------------------------------------------------
    //TODO: check range issue.
    @SuppressLint("Range")
    class MediaContentDao(private val mBaseUrl: String) : IMediaContentDao {
        override fun getImageItems(context: Context): List<Item> {
            val items: MutableList<Item> = ArrayList()
            context.contentResolver.query(Media.EXTERNAL_CONTENT_URI, CONTENT_IMAGE_COLUMNS, null, null, null)
                .use { cursor ->
                    if (cursor == null) return items
                    cursor.moveToFirst()
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[0])).toString()
                        val title = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[1]))
                        val data = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[2]))
                        val mimeType = cursor.getString(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[3]))
                        val size = cursor.getLong(cursor.getColumnIndex(CONTENT_IMAGE_COLUMNS[4]))
                        val url = mBaseUrl + data
                        val res = Res(mimeType, size, "", null, url)
                        val imageItem = ImageItem(id, IMAGE.id, title, "", res)
                        items.add(imageItem)
                    }
                }
            return items
        }

        override fun getAudioItems(context: Context): List<Item> {
            val items: MutableList<Item> = ArrayList()
            context.contentResolver.query(Audio.Media.EXTERNAL_CONTENT_URI, CONTENT_AUDIO_COLUMNS, null, null, null)
                .use { cursor ->
                    if (cursor == null) return items
                    cursor.moveToFirst()
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[0])).toString()
                        val title = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[1]))
                        val creator = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[2]))
                        val data = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[3]))
                        val mimeType = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[4]))
                        val size = cursor.getLong(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[5]))
                        // long duration = cursor.getLong(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[6]));
                        // String durationStr = ModelUtil.toTimeString(duration / 1000);
                        val album = cursor.getString(cursor.getColumnIndex(CONTENT_AUDIO_COLUMNS[6]))
                        val url = mBaseUrl + data
                        val res = Res(mimeType, size, "", null, url)
                        val musicTrack = MusicTrack(id, AUDIO.id, title, creator, album, PersonWithRole(creator), res)
                        items.add(musicTrack)
                    }
                }
            return items
        }

        override fun getVideoItems(context: Context): List<Item> {
            val items: MutableList<Item> = ArrayList()
            context.contentResolver.query(Video.Media.EXTERNAL_CONTENT_URI, CONTENT_VIDEO_COLUMNS, null, null, null)
                .use { cur ->
                    if (cur == null) return items
                    cur.moveToFirst()
                    while (cur.moveToNext()) {
                        val id = cur.getInt(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[0])).toString()
                        val title = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[1]))
                        val creator = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[2]))
                        val data = cur.getString(cur.getColumnIndexOrThrow(CONTENT_VIDEO_COLUMNS[3]))
                        val mimeType = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[4]))
                        val size = cur.getLong(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[5]))
                        // long duration = cur.getLong(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[6]));
                        // String durationStr = ModelUtil.toTimeString(duration / 1000);
                        val resolution = cur.getString(cur.getColumnIndex(CONTENT_VIDEO_COLUMNS[6]))
                        val url = mBaseUrl + data
                        val res = Res(mimeType, size, "", null, url)
                        res.resolution = resolution
                        val movie = Movie(id, VIDEO.id, title, creator, res)
                        items.add(movie)
                    }
                }
            return items
        }

    }
}