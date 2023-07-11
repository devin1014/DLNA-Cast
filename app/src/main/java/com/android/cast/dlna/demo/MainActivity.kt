package com.android.cast.dlna.demo

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.cast.dlna.demo.DeviceAdapter.OnItemSelectedListener
import com.android.cast.dlna.demo.Utils.Companion.getWiFiInfoSSID
import com.android.cast.dlna.dmc.DLNACastManager
import com.permissionx.guolindev.PermissionX
import org.fourthline.cling.model.meta.Device

class MainActivity : AppCompatActivity() {

    private lateinit var deviceListAdapter: DeviceAdapter
    private lateinit var informationFragment: Fragment
    private lateinit var queryFragment: Fragment
    private lateinit var controlFragment: Fragment
    private lateinit var localControlFragment: Fragment

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
        informationFragment = supportFragmentManager.findFragmentById(R.id.fragment_information)!!
        queryFragment = supportFragmentManager.findFragmentById(R.id.fragment_query)!!
        controlFragment = supportFragmentManager.findFragmentById(R.id.fragment_control)!!
        localControlFragment = supportFragmentManager.findFragmentById(R.id.fragment_local_provider)!!

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
                deviceListAdapter.castDevice = if (selected) castDevice else null
                (informationFragment as? IDisplayDevice)?.setCastDevice(if (selected) castDevice else null)
                (controlFragment as? IDisplayDevice)?.setCastDevice(if (selected) castDevice else null)
                (localControlFragment as? IDisplayDevice)?.setCastDevice(if (selected) castDevice else null)
                (queryFragment as? IDisplayDevice)?.setCastDevice(if (selected) castDevice else null)
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
                    R.id.cast_type_info -> {
                        this
                            .show(informationFragment)
                            .hide(controlFragment)
                            .hide(localControlFragment)
                            .hide(queryFragment)
                    }

                    R.id.cast_type_query -> {
                        this
                            .show(queryFragment)
                            .hide(informationFragment)
                            .hide(controlFragment)
                            .hide(localControlFragment)
                    }

                    R.id.cast_type_ctrl -> {
                        this
                            .show(controlFragment)
                            .hide(localControlFragment)
                            .hide(informationFragment)
                            .hide(queryFragment)
                    }

                    R.id.cast_type_ctrl_local -> {
                        this
                            .show(localControlFragment)
                            .hide(controlFragment)
                            .hide(informationFragment)
                            .hide(queryFragment)
                    }
                }
            }
            .commit()
    }
}