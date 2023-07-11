package com.android.cast.dlna.demo.renderer

import android.os.Bundle
import android.widget.ImageView
import com.android.cast.dlna.dmr.service.keyCurrentURI

class ImageRendererActivity : BaseRendererActivity() {

    private val imageView: ImageView by lazy { findViewById(R.id.image) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_renderer)

        val url = intent.getStringExtra(keyCurrentURI)
//        imageView.setImageURI(Uri.parse())
//        Glide.with(fragment)
//            .load(url)
//            .into(imageView);
    }
}