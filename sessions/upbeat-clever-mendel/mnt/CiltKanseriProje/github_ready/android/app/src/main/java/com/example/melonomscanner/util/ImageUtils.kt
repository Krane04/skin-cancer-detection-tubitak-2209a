package com.example.melonomscanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Görüntü dosyaları için yardımcılar:
 *  - EXIF'e göre rotation düzeltme,
 *  - Uygulama iç depolama alanına JPEG olarak kaydetme,
 *  - Dosya yolu üretme.
 *
 * Tüm dosyalar uygulamanın özel filesDir altında tutulur — KVKK uyumu için
 * harici depolamaya yazılmaz.
 */
object ImageUtils {

    private const val SCANS_DIR = "scans"
    private const val HEATMAPS_DIR = "heatmaps"
    private const val JPEG_QUALITY = 92

    fun scansDir(context: Context): File =
        File(context.filesDir, SCANS_DIR).apply { if (!exists()) mkdirs() }

    fun heatmapsDir(context: Context): File =
        File(context.filesDir, HEATMAPS_DIR).apply { if (!exists()) mkdirs() }

    fun newScanFile(context: Context): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(scansDir(context), "scan_$stamp.jpg")
    }

    fun newHeatmapFile(context: Context): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(heatmapsDir(context), "heatmap_$stamp.jpg")
    }

    /** Bitmap'i dosyaya yazar, path döndürür. */
    fun saveJpeg(bitmap: Bitmap, target: File, quality: Int = JPEG_QUALITY): String {
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        return target.absolutePath
    }

    fun loadBitmap(path: String, maxDim: Int = 1280): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        val sample = calculateInSampleSize(opts.outWidth, opts.outHeight, maxDim)
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bmp = BitmapFactory.decodeFile(path, decodeOpts) ?: return null
        return applyExifRotation(path, bmp)
    }

    private fun calculateInSampleSize(width: Int, height: Int, max: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= max || h / 2 >= max) {
            w /= 2; h /= 2; sample *= 2
        }
        return sample
    }

    private fun applyExifRotation(path: String, bmp: Bitmap): Bitmap {
        return runCatching {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                else -> return bmp
            }
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }.getOrDefault(bmp)
    }

    /** Merkezden kare kırp ve boyutlandır. YOLO girişine hazırlarken kullanılır. */
    fun centerCropSquare(bitmap: Bitmap, size: Int): Bitmap {
        val dim = minOf(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - dim) / 2
        val yOffset = (bitmap.height - dim) / 2
        val cropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, dim, dim)
        return if (cropped.width == size) cropped
               else Bitmap.createScaledBitmap(cropped, size, size, true)
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
