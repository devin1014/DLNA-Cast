package com.android.cast.dlna.demo

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.demo.Utils.Companion.getWiFiInfoSSID
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
    }

    private fun resetToolbar() {
        supportActionBar?.title = "DLNA Cast"
        supportActionBar?.subtitle = getWiFiInfoSSID(this)
    }

    override fun onStart() {
        super.onStart()
        resetToolbar()
        DLNACastManager.bindCastService(this)
    }

    override fun onItemClick(device: Device<*, *, *>) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.detail_container, DetailFragment.create(device))
            .commit()
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
            Toast.makeText(this, "开始搜索", Toast.LENGTH_SHORT).show()
            DLNACastManager.search(null, 60)
        }
        return super.onOptionsItemSelected(item)
    }

}