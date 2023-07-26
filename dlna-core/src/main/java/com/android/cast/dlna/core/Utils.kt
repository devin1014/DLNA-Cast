/*
 * Copyright (C) 2014 Kevin Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.cast.dlna.core

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.Video
import java.util.Formatter
import java.util.Locale

object Utils {
    // ------------------------------------------------------------------------------------------------------------------------
    // ---- Device Wifi Information
    // ------------------------------------------------------------------------------------------------------------------------
    private const val NETWORK_TYPE_WIFI = "WiFi"
    private const val NETWORK_TYPE_MOBILE = "Mobile EDGE>"
    private const val NETWORK_TYPE_OTHERS = "Others>"
    private const val UNKNOWN = "<unknown>"
    fun getWiFiInfoIPAddress(context: Context): String {
        val wifiManager = getSystemService<WifiManager>(context, Context.WIFI_SERVICE)
        if (!wifiManager.isWifiEnabled) return ""
        val wifiInfo = wifiManager.connectionInfo ?: return UNKNOWN
        val address = wifiInfo.ipAddress
        return if (address == 0) UNKNOWN else (address and 0xFF).toString() + "." + (address shr 8 and 0xFF) + "." + (address shr 16 and 0xFF) + "." + (address shr 24 and 0xFF)
    }

    fun getActiveNetworkInfo(context: Context): String {
        val connectivityManager = getSystemService<ConnectivityManager?>(context, Context.CONNECTIVITY_SERVICE) ?: return UNKNOWN
        val networkInfo = connectivityManager.activeNetworkInfo ?: return UNKNOWN
        return when (networkInfo.type) {
            ConnectivityManager.TYPE_WIFI -> NETWORK_TYPE_WIFI
            ConnectivityManager.TYPE_MOBILE -> NETWORK_TYPE_MOBILE
            else -> NETWORK_TYPE_OTHERS
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any?> getSystemService(context: Context, name: String): T {
        return context.applicationContext.getSystemService(name) as T
    }
    // ------------------------------------------------------------------------------------------------------------------------
    // ---- Time&Date Format
    // ------------------------------------------------------------------------------------------------------------------------
    /**
     * 把时间戳转换成 00:00:00 格式
     *
     * @param timeMs 时间戳
     * @return 00:00:00 时间格式
     */
    @JvmStatic
    fun getStringTime(timeMs: Long): String {
        val formatBuilder = StringBuilder()
        val formatter = Formatter(formatBuilder, Locale.US)
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString()
    }

    /**
     * 把 00:00:00 格式转成时间戳
     *
     * @param formatTime 00:00:00 时间格式
     * @return 时间戳(毫秒)
     */
    fun getIntTime(formatTime: String?): Long {
        if (!formatTime.isNullOrEmpty()) {
            val tmp = formatTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (tmp.size < 3) {
                return 0
            }
            val second = tmp[0].toInt() * 3600 + tmp[1].toInt() * 60 + tmp[2].toInt()
            return second * 1000L
        }
        return 0
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // ---- Others
    // ------------------------------------------------------------------------------------------------------------------------
    fun parseUri2Path(context: Context, uri: Uri?): String? {
        if (uri == null) return null
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                //TODO: put file into 'Download' dir, can not get valid document id.
                //content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Foceans.mp4
                val id = DocumentsContract.getDocumentId(uri).let {
                    try {
                        it.toLong()
                    } catch (_: Exception) {
                        it.hashCode().toLong()
                    }
                }
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id)
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     */
    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var path: String? = null
        val projection = arrayOf(Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                path = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            cursor?.close()
        }
        return path
    }
}