package com.android.cast.dlna.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.cast.dlna.dms.DLNAContentService

class ContentServerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_server)
        DLNAContentService.startService(this)
    }
}