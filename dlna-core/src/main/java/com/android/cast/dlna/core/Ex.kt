package com.android.cast.dlna.core

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import org.fourthline.cling.model.meta.Icon
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun Bitmap.toIcon(
    width: Int = 48,
    height: Int = 48,
    depth: Int = 8
): Icon {
    val stream = ByteArrayOutputStream()
    this.compress(PNG, 100, stream)
    return Icon("image/png", width, height, depth, "icon.png", ByteArrayInputStream(stream.toByteArray()))
}