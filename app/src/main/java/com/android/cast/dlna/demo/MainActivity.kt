package com.android.cast.dlna.demo

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.cast.dlna.demo.DeviceAdapter.OnItemSelectedListener
import com.android.cast.dlna.demo.Utils.Companion.getWiFiInfoSSID
import com.android.cast.dlna.demo.fragment.ControlFragment
import com.android.cast.dlna.demo.fragment.InfoFragment
import com.android.cast.dlna.demo.fragment.LocalControlFragment
import com.android.cast.dlna.demo.fragment.QueryFragment
import com.android.cast.dlna.dmc.DLNACastManager
import com.permissionx.guolindev.PermissionX
import org.fourthline.cling.model.meta.Device

class MainActivity : AppCompatActivity() {

    private lateinit var deviceListAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponent()
        PermissionX.init(this)
            .permissions(permission.READ_EXTERNAL_STORAGE, permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION)
            .request { _: Boolean, _: List<String?>?, _: List<String?>? -> resetToolbar() }
    }

    private fun initComponent() {
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<RadioGroup>(R.id.cast_type_group).apply {
            setOnCheckedChangeListener(checkedChangeListener)
            check(R.id.cast_type_info)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.cast_device_list)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = DeviceAdapter(this, object : OnItemSelectedListener {
            override fun onItemSelected(castDevice: Device<*, *, *>?, selected: Boolean) {
                val device = if (selected) castDevice else null
                deviceListAdapter.castDevice = device
                (supportFragmentManager.findFragmentById(R.id.fragment_container) as IDisplayDevice).setCastDevice(device)
            }
        }).also { deviceListAdapter = it }

        DLNACastManager.registerDeviceListener(deviceListAdapter)
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

    override fun onStop() {
        DLNACastManager.unbindCastService(this)
        super.onStop()
    }

    override fun onDestroy() {
        DLNACastManager.unregisterListener(deviceListAdapter)
        super.onDestroy()
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

    private val checkedChangeListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        supportFragmentManager.beginTransaction()
            .apply {
                when (checkedId) {
                    R.id.cast_type_info -> replace(R.id.fragment_container, InfoFragment())
                    R.id.cast_type_query -> replace(R.id.fragment_container, QueryFragment())
                    R.id.cast_type_ctrl -> replace(R.id.fragment_container, ControlFragment())
                    R.id.cast_type_ctrl_local -> replace(R.id.fragment_container, LocalControlFragment())
                }
            }
            .commit()
    }
}