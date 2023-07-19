package com.android.cast.dlna.demo.renderer

import android.os.Bundle
import android.widget.ImageView
import com.android.cast.dlna.dmr.BaseRendererActivity

class ImageRendererActivity : BaseRendererActivity() {

    private val imageView: ImageView by lazy { findViewById(R.id.image) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_renderer)
        // ...
    }
}