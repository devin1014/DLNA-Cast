package com.android.cast.dlna.demo.renderer.util

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.telephony.TelephonyManager
import android.view.InputDevice


object Utils {
    fun isTelevision(context: Context): Boolean {
        // 1. 检查是否声明为电视设备 (首选方法)
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            return true
        }

        // 2. 检查是否支持电话功能 (电视通常不支持)
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            return false // 具备通话功能，肯定不是电视
        }

        // 3. 检查输入方式 (电视主要依赖遥控器/DPAD)
        val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
        val inputDeviceIds = inputManager.inputDeviceIds
        var hasDpad = false
        var hasTouchScreen = false

        for (id in inputDeviceIds) {
            val device = inputManager.getInputDevice(id)
            if ((device!!.sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
                hasDpad = true
            }
            if ((device.sources and InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN) {
                hasTouchScreen = true
            }
        }
        // 如果有DPAD但没有触摸屏，很可能是电视
        if (hasDpad && !hasTouchScreen) {
            return true
        }

        // 4. 通过制造商和型号辅助判断 (应对厂商定制系统)
//        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
//        val model = Build.MODEL.lowercase(Locale.getDefault())
//        if (manufacturer.contains("xiaomi") && model.contains("tv")) {
//            return true
//        }
//        if (manufacturer.contains("hisense") || manufacturer.contains("skyworth") || manufacturer.contains("tcl")) {
//            return true
//        }

        return false
    }
}