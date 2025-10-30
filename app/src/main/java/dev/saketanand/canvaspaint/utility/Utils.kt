package dev.saketanand.canvaspaint.utility

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream

fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = createBitmap(
        picture.width, picture.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    picture.draw(canvas)
    return bitmap
}

suspend fun saveBitmapToStorage(
    context: Context, bitmap: Bitmap, displayName: String
): Boolean = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.WIDTH, bitmap.width)
        put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1) // Use 1 for pending
        }
    }
    var uri: Uri? = null
    var stream: OutputStream? = null
    try {
        uri = resolver.insert(imageCollection, contentValues)
        if (uri == null) {
            Log.e("SaveBitmap", "Failed to create new MediaStore entry.")
            return@withContext false
        }

        stream = resolver.openOutputStream(uri)
        if (stream == null) {
            Log.e("SaveBitmap", "Failed to open output stream for $uri.")
            return@withContext false
        }

        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
            Log.e("SaveBitmap", "Failed to save bitmap.")
            return@withContext false
        }

        // If on API 29+, mark the image as no longer pending
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        Log.d("SaveBitmap", "Bitmap saved successfully to $uri")
        return@withContext true // Success!

    } catch (e: Exception) {
        Log.e("SaveBitmap", "Error saving bitmap", e)

        if (uri != null) {
            try {
                resolver.delete(uri, null, null)
            } catch (deleteException: Exception) {
                Log.e("SaveBitmap", "Error deleting incomplete entry $uri", deleteException)
            }
        }

        return@withContext false // Failure

    } finally {
        // Always close the stream in the 'finally' block
        //    to prevent resource leaks.
        try {
            stream?.close()
        } catch (e: IOException) {
            Log.e("SaveBitmap", "Error closing output stream", e)
        }
    }
}