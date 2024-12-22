package com.application.mystoryapp

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
private val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

fun getImageUri(context: Context): Uri {
    var uri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCamera/")
        }
        uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    return uri ?: getImageUriForPreQ(context)
}

private fun getImageUriForPreQ(context: Context): Uri {
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(filesDir, "/MyCamera/$timeStamp.jpg")
    if (imageFile.parentFile?.exists() == false) imageFile.parentFile?.mkdir()
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        imageFile
    )
}

fun uriToFile(uri: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)

    val inputStream: InputStream? = contentResolver.openInputStream(uri)
    val outputStream: FileOutputStream = FileOutputStream(tempFile)

    inputStream.use { input ->
        outputStream.use { output ->
            input?.copyTo(output)
        }
    }

    return tempFile
}

fun File.reduceFileImage(maxSize: Int = 1_000_000): File {
    // Decode the image file into a Bitmap
    val bitmap = BitmapFactory.decodeFile(this.path)

    var compressQuality = 100
    var streamLength: Int

    do {
        val outputStream = FileOutputStream(this)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, outputStream)
        outputStream.flush()
        outputStream.close()
        streamLength = this.length().toInt()
        compressQuality -= 5
    } while (streamLength > maxSize && compressQuality > 0)

    return this
}