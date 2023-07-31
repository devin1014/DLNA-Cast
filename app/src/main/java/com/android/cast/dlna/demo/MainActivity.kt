package com.android.cast.dlna.demo

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.cast.dlna.core.Utils
import com.android.cast.dlna.demo.fragment.OnItemClickListener
import com.android.cast.dlna.dmc.DLNACastManager
import com.permissionx.guolindev.PermissionX
import org.fourthline.cling.model.meta.Device

class MainActivity : AppCompatActivity(), OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        PermissionX.init(this)
            .permissions(permission.READ_EXTERNAL_STORAGE, permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION)
            .request { _: Boolean, _: List<String?>?, _: List<String?>? -> resetToolbar() }
        DLNACastManager.bindCastService(this)
    }

    private fun resetToolbar() {
        supportActionBar?.title = "DLNA Cast"
        supportActionBar?.subtitle = "${getWiFiInfoSSID(this)} - ${Utils.getWiFiInfoIPAddress(this)}"
    }

    override fun onStart() {
        super.onStart()
        resetToolbar()
    }

    override fun onDestroy() {
        DLNACastManager.unbindCastService(this)
        super.onDestroy()
    }

    override fun onItemClick(device: Device<*, *, *>) {
        replace(R.id.detail_container, DetailFragment.create(device))
    }

    override fun onBackPressed() {
        val detailFragment = supportFragmentManager.findFragmentById(R.id.detail_container)
        if (detailFragment != null) {
            supportFragmentManager.beginTransaction().remove(detailFragment).commit()
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_search_start) {
            Toast.makeText(this, "开始搜索...", Toast.LENGTH_SHORT).show()
//            DLNACastManager.search(DLNACastManager.DEVICE_TYPE_MEDIA_RENDERER, 60)
            DLNACastManager.search()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val WIFI_DISABLED = "<disabled>"
        private const val WIFI_NO_CONNECT = "<not connect>"
        private const val WIFI_NO_PERMISSION = "<permission deny>"
        private const val UNKNOWN = "<unknown>"
    }

    /**
     * need permission 'Manifest.permission.ACCESS_FINE_LOCATION' and 'Manifest.permission.ACCESS_WIFI_STATE' if system sdk >= Android O.
     */
    @SuppressLint("MissingPermission")
    private fun getWiFiInfoSSID(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) return WIFI_DISABLED
        val wifiInfo = wifiManager.connectionInfo ?: return WIFI_NO_CONNECT
        return if (wifiInfo.ssid == WifiManager.UNKNOWN_SSID) {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
                    return WIFI_NO_PERMISSION
                }
                if (wifiManager.configuredNetworks != null) {
                    for (config in wifiManager.configuredNetworks) {
                        if (config.networkId == wifiInfo.networkId) {
                            return config.SSID.replace("\"".toRegex(), "")
                        }
                    }
                }
            } else {
                return WIFI_NO_CONNECT
            }
            UNKNOWN
        } else {
            wifiInfo.ssid.replace("\"".toRegex(), "")
        }
    }
}