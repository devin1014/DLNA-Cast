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
package com.android.cast.dlna.demo.renderer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.core.app.ActivityCompat;

class Utils {

    public static String getWiFiSSID(Context context) {
        WifiManager wifiManager = getSystemService(context, Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSSID().equals(WifiManager.UNKNOWN_SSID)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "No 'ACCESS_FINE_LOCATION' Permission";
            }
            if (wifiManager.getConfiguredNetworks() != null) {
                for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
                    if (config.networkId == wifiInfo.getNetworkId()) {
                        return String.format("WIFI: %s", config.SSID.replaceAll("\"", ""));
                    }
                }
            }
            return String.format("WIFI: %s", WifiManager.UNKNOWN_SSID);
        } else {
            return String.format("WIFI: %s", wifiInfo.getSSID().replaceAll("\"", ""));
        }
    }

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
}
