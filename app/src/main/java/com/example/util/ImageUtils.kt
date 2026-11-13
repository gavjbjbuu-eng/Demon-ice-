package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {
    suspend fun uriToCompressedBase64(
        context: Context,
        uri: Uri,
        maxDimension: Int = 512,
        quality: Int = 72
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val input = context.contentResolver.openInputStream(uri) ?: return@runCatching null
            val original = input.use { BitmapFactory.decodeStream(it) } ?: return@runCatching null
            val ratio = minOf(
                maxDimension.toFloat() / original.width,
                maxDimension.toFloat() / original.height,
                1f
            )
            val targetW = (original.width * ratio).toInt().coerceAtLeast(1)
            val targetH = (original.height * ratio).toInt().coerceAtLeast(1)
            val scaled = if (targetW != original.width || targetH != original.height) {
                Bitmap.createScaledBitmap(original, targetW, targetH, true)
            } else {
                original
            }
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val bytes = baos.toByteArray()
            if (scaled != original) scaled.recycle()
            original.recycle()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }.getOrNull()
    }

    fun decodeBase64ImageBitmap(base64: String?): ImageBitmap? {
        if (base64.isNullOrBlank()) return null
        return runCatching {
            val clean = base64.substringAfter("base64,", base64)
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
}
