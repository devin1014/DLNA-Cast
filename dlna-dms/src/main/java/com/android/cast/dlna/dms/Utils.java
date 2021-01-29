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
package com.android.cast.dlna.dms;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

public class Utils {

    //TODO:check auth or multiple ip
    public static String getWiFiIPAddress(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int address = wifiInfo.getIpAddress();
            return (address & 0xFF) + "." + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF) + "." + (address >> 24 & 0xFF);
        } else {
            return "unknown";
        }
    }

    @SuppressWarnings({"unchecked", "TypeParameterExplicitlyExtendsObject", "SameParameterValue"})
    private static <T extends Object> T getSystemService(Context context, String name) {
        return (T) context.getApplicationContext().getSystemService(name);
    }

    // /**
    //  * Returns MAC address of the given interface name.
    //  *
    //  * @param interfaceName eth0, wlan0 or NULL=use first interface
    //  * @return mac address or empty string
    //  */
    // public static String getMACAddress(String interfaceName) {
    //     try {
    //         List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
    //         for (NetworkInterface intf : interfaces) {
    //             if (interfaceName != null) {
    //                 if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
    //             }
    //             byte[] mac = intf.getHardwareAddress();
    //             if (mac == null) return "";
    //             StringBuilder buf = new StringBuilder();
    //             for (int idx = 0; idx < mac.length; idx++)
    //                 buf.append(String.format("%02X:", mac[idx]));
    //             if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
    //             return buf.toString();
    //         }
    //     } catch (Exception ex) {
    //     } // for now eat exceptions
    //     return "";
    // }

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    // public static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
    //     String filePath = null;
    //     if (DocumentsContract.isDocumentUri(context, uri)) { // 如果是document类型的 uri, 则通过document id来进行处理
    //         String documentId = DocumentsContract.getDocumentId(uri);
    //         if (isMediaDocument(uri)) { // MediaProvider, 使用':'分割
    //             String id = documentId.split(":")[1];
    //             String selection = MediaStore.Images.Media._ID + "=?";
    //             String[] selectionArgs = {id};
    //             filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
    //         } else if (isDownloadsDocument(uri)) { // DownloadsProvider
    //             Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(documentId));
    //             filePath = getDataColumn(context, contentUri, null, null);
    //         }
    //     } else if ("content".equalsIgnoreCase(uri.getScheme())) { // 如果是 content 类型的 Uri
    //         filePath = getDataColumn(context, uri, null, null);
    //     } else if ("file".equals(uri.getScheme())) { // 如果是 file 类型的 Uri,直接获取图片对应的路径
    //         filePath = uri.getPath();
    //     }
    //     return filePath;
    // }

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

    // public static boolean isLocalIpAddress(String checkip) {
    //     boolean ret = false;
    //     if (checkip != null) {
    //         try {
    //             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
    //                 NetworkInterface intf = en.nextElement();
    //                 for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
    //                     InetAddress inetAddress = enumIpAddr.nextElement();
    //                     if (!inetAddress.isLoopbackAddress()) {
    //                         String ip = inetAddress.getHostAddress().toString();
    //                         if (checkip.equals(ip)) {
    //                             return true;
    //                         }
    //                     }
    //                 }
    //             }
    //         } catch (SocketException ex) {
    //             ex.printStackTrace();
    //         }
    //     }
    //
    //     return ret;
    // }
    //
    // public static String getIP() throws SocketException {
    //     String ipaddress = "";
    //     for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
    //         NetworkInterface intf = en.nextElement();
    //         if (intf.getName().toLowerCase().equals("eth0") || intf.getName().toLowerCase().equals("wlan0")) {
    //             for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
    //                 InetAddress inetAddress = enumIpAddr.nextElement();
    //                 if (!inetAddress.isLoopbackAddress()) {
    //                     ipaddress = inetAddress.getHostAddress().toString();
    //                     if (!ipaddress.contains("::")) {// ipV6的地址
    //                         Log.e(TAG, ipaddress);
    //                         return ipaddress;
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     return ipaddress;
    // }
    //
    // /**
    //  * Get IP address from first non-localhost interface
    //  *
    //  * @param useIPv4 true=return ipv4, false=return ipv6
    //  * @return address or empty string
    //  */
    // public static String getIPAddress(boolean useIPv4) {
    //     try {
    //         List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
    //         for (NetworkInterface intf : interfaces) {
    //             List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
    //             for (InetAddress addr : addrs) {
    //                 if (!addr.isLoopbackAddress()) {
    //                     String sAddr = addr.getHostAddress().toUpperCase();
    //                     boolean isIPv4 = addr instanceof Inet4Address;
    //                     if (useIPv4) {
    //                         if (isIPv4)
    //                             return sAddr;
    //                     } else {
    //                         if (!isIPv4) {
    //                             int delim = sAddr.indexOf('%'); // drop ip6 port suffix
    //                             return delim < 0 ? sAddr : sAddr.substring(0, delim);
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //     } catch (Exception ex) {
    //     } // for now eat exceptions
    //     return "";
    // }
}
