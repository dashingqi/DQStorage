package com.dashingqi.applicationa

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.annotation.NonNull
import java.io.*

/**
 * MediaStore 工具类
 * @author zhangqi61
 * @since 2022/10/14
 */


/**
 * 扫描媒体文件
 * @param context Context 上下文环境
 */
fun scanMedia(@NonNull context: Context, imageView: ImageView) {
    val resolver = context.contentResolver ?: return
    val mediaColumns = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.TITLE,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATE_EXPIRES,
        MediaStore.Images.Media.DURATION,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT
    )
    var cursor: Cursor? = null
    try {
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaColumns,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED
        ) ?: return
        val data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val mimeType = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val size = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val title = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
        val width = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val contentUri = getContentUri(0, "$id") ?: return

        Log.d(
            TAG, """
            data === $data
            mimeType === $mimeType
            id === $id
            size === $size
            title === $title
            contentUri == $contentUri
            width == $width
        """.trimIndent()
        )

        val fileInput = getFileInput(context, contentUri)
        val realBitmap = BitmapFactory.decodeStream(fileInput)
        Log.d(TAG, "byte count  == ${realBitmap.byteCount}")
        imageView.setImageBitmap(realBitmap)
    } catch (exception: Exception) {
        exception.printStackTrace()
    } finally {
        cursor?.close()
    }
}

fun getContentUri(type: Int, id: String): Uri? {
    return when (type) {
        0 -> {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build()
        }
        1 -> {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build()
        }
        2 -> {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id).build()
        }
        else -> null

    }
}

fun getFileInput(@NonNull context: Context, @NonNull uri: Uri): InputStream? {
    val resolver = context.contentResolver
    runCatching {
        val openAssetFileDescriptor = resolver.openAssetFileDescriptor(uri, "r") ?: return null
        val parcelFileDescriptor = openAssetFileDescriptor.parcelFileDescriptor
        if (parcelFileDescriptor != null) {
            return FileInputStream(parcelFileDescriptor.fileDescriptor)
        }
    }
    return null
}


fun insertImageIntoMediaStore(context: Context, fileName: String, mimeType: String): Uri? {
    val contentValues = ContentValues()
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
    contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

fun copyImageToPublicDir(@NonNull context: Context, @NonNull file: File) {
    if (file.exists() && file.isFile) {
        val uri = insertImageIntoMediaStore(context, file.name, "application/image") ?: return
        val fileDescriptor: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "w")
        fileDescriptor ?: return
        writeToPublicDir(file, fileDescriptor)
    }
}

fun writeToPublicDir(file: File, fileDescriptor: ParcelFileDescriptor) {
    runCatching {
        val fis = FileInputStream(file)
        val fos = FileOutputStream(fileDescriptor.fileDescriptor)
        val readBytes = fis.readBytes()
        fos.write(readBytes)
    }.onFailure {
        it.printStackTrace()
    }
}