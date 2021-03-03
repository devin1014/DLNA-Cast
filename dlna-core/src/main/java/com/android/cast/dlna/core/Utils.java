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
package com.android.cast.dlna.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class Utils {

    // ------------------------------------------------------------------------------------------------------------------------
    // ---- Metadata
    // ------------------------------------------------------------------------------------------------------------------------
    private static final String DIDL_LITE_FOOTER = "</DIDL-Lite>";
    private static final String DIDL_LITE_HEADER = "<?xml version=\"1.0\"?>" + "<DIDL-Lite " + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">";
    private static final String CAST_PARENT_ID = "1";
    private static final String CAST_CREATOR = "NLUpnpCast";
    private static final String CAST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(CAST_DATE_FORMAT, Locale.US);

    public static String getMetadata(ICast cast) {
        if (cast instanceof ICast.ICastVideo) {
            ICast.ICastVideo castObj = (ICast.ICastVideo) cast;
            Res castRes = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), castObj.getSize(), cast.getUri());
            castRes.setBitrate(castObj.getBitrate());
            castRes.setDuration(com.android.cast.dlna.core.Utils.getStringTime(castObj.getDurationMillSeconds()));
            String resolution = "description";
            VideoItem item = new VideoItem(cast.getId(), CAST_PARENT_ID, cast.getName(), CAST_CREATOR, castRes);
            item.setDescription(resolution);
            return createItemMetadata(item);
        }
        if (cast instanceof ICast.ICastAudio) {
            ICast.ICastAudio castObj = (ICast.ICastAudio) cast;
            Res castRes = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), castObj.getSize(), cast.getUri());
            castRes.setDuration(com.android.cast.dlna.core.Utils.getStringTime(castObj.getDurationMillSeconds()));
            String resolution = "description";
            AudioItem item = new AudioItem(cast.getId(), CAST_PARENT_ID, cast.getName(), CAST_CREATOR, castRes);
            item.setDescription(resolution);
            return createItemMetadata(item);
        } else if (cast instanceof ICast.ICastImage) {
            ICast.ICastImage castObj = (ICast.ICastImage) cast;
            Res castRes = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), castObj.getSize(), cast.getUri());
            String resolution = "description";
            ImageItem item = new ImageItem(cast.getId(), CAST_PARENT_ID, cast.getName(), CAST_CREATOR, castRes);
            item.setDescription(resolution);
            return createItemMetadata(item);
        } else {
            return "";
        }
    }

    private static String createItemMetadata(DIDLObject item) {
        StringBuilder metadata = new StringBuilder();
        metadata.append(DIDL_LITE_HEADER);
        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.getId(), item.getParentID(), item.isRestricted() ? "1" : "0"));
        metadata.append(String.format("<dc:title>%s</dc:title>", item.getTitle()));
        String creator = item.getCreator();
        if (creator != null) {
            creator = creator.replaceAll("<", "_");
            creator = creator.replaceAll(">", "_");
        }
        metadata.append(String.format("<upnp:artist>%s</upnp:artist>", creator));
        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.getClazz().getValue()));
        metadata.append(String.format("<dc:date>%s</dc:date>", DATE_FORMAT.format(new Date())));

        // metadata.append(String.format("<upnp:album>%s</upnp:album>",item.get);
        // <res protocolInfo="http-get:*:audio/mpeg:*"
        // resolution="640x478">http://192.168.1.104:8088/Music/07.我醒著做夢.mp3</res>

        Res res = item.getFirstResource();
        if (res != null) {
            // protocol info
            String protocolInfo = "";
            ProtocolInfo pi = res.getProtocolInfo();
            if (pi != null) {
                protocolInfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.getProtocol(), pi.getNetwork(), pi.getContentFormatMimeType(), pi.getAdditionalInfo());
            }

            // resolution, extra info, not adding yet
            String resolution = "";
            if (!TextUtils.isEmpty(res.getResolution())) {
                resolution = String.format("resolution=\"%s\"", res.getResolution());
            }

            // duration
            String duration = "";
            if (!TextUtils.isEmpty(res.getDuration())) {
                duration = String.format("duration=\"%s\"", res.getDuration());
            }

            // res begin
            // metadata.append(String.format("<res %s>", protocolInfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolInfo, resolution, duration));

            // url
            metadata.append(res.getValue());

            // res end
            metadata.append("</res>");
        }
        metadata.append("</item>");

        metadata.append(DIDL_LITE_FOOTER);

        return metadata.toString();
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // ---- Device Wifi Information
    // ------------------------------------------------------------------------------------------------------------------------
    private static final String NETWORK_TYPE_WIFI = "WiFi";
    private static final String NETWORK_TYPE_MOBILE = "Mobile EDGE>";
    private static final String NETWORK_TYPE_OTHERS = "Others>";

    private static final String WIFI_DISABLED = "<disabled>";
    private static final String WIFI_NO_CONNECT = "<not connect>";
    private static final String WIFI_NO_PERMISSION = "<permission deny>";

    private static final String UNKNOWN = "<unknown>";

    /**
     * need permission 'Manifest.permission.ACCESS_FINE_LOCATION' and 'Manifest.permission.ACCESS_WIFI_STATE' if system sdk >= Android O.
     */
    @SuppressLint("MissingPermission")
    public static String getWiFiInfoSSID(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) return WIFI_DISABLED;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) return WIFI_NO_CONNECT;
        if (wifiInfo.getSSID().equals(WifiManager.UNKNOWN_SSID)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return WIFI_NO_PERMISSION;
                }
                if (wifiManager.getConfiguredNetworks() != null) {
                    for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
                        if (config.networkId == wifiInfo.getNetworkId()) {
                            return config.SSID.replaceAll("\"", "");
                        }
                    }
                }
            } else {
                return WIFI_NO_CONNECT;
            }
            return UNKNOWN;
        } else {
            return wifiInfo.getSSID().replaceAll("\"", "");
        }
    }

    public static String getWiFiInfoIPAddress(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        if (wifiManager == null || !wifiManager.isWifiEnabled()) return "";
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) return UNKNOWN;
        int address = wifiInfo.getIpAddress();
        if (address == 0) return UNKNOWN;
        return (address & 0xFF) + "." + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF) + "." + (address >> 24 & 0xFF);
    }

    public static String getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = getSystemService(context, Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return UNKNOWN;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) return UNKNOWN;
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_TYPE_WIFI;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return NETWORK_TYPE_MOBILE;
        } else {
            return NETWORK_TYPE_OTHERS;
        }
    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject", "SameParameterValue"})
    private static <T extends Object> T getSystemService(Context context, String name) {
        return (T) context.getApplicationContext().getSystemService(name);
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
    public static String getStringTime(long timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.US);

        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }

    /**
     * 把 00:00:00 格式转成时间戳
     *
     * @param formatTime 00:00:00 时间格式
     * @return 时间戳(毫秒)
     */
    public static long getIntTime(String formatTime) {
        if (!TextUtils.isEmpty(formatTime)) {
            String[] tmp = formatTime.split(":");

            if (tmp.length < 3) {
                return 0;
            }

            int second = Integer.parseInt(tmp[0]) * 3600 + Integer.parseInt(tmp[1]) * 60 + Integer.parseInt(tmp[2]);

            return second * 1000L;
        }

        return 0;
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // ---- Others
    // ------------------------------------------------------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String parseUri2Path(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    public static String toHexString(int hashCode) {
        return Integer.toHexString(hashCode).toUpperCase(Locale.US);
    }
}
