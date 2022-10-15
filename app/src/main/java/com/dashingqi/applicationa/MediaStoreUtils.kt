package com.dashingqi.applicationa

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
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

/**
 * 扫描媒体文件
 * @param context Context 上下文环境
 */
fun scanImageMedia(@NonNull context: Context, @NonNull imageView: ImageView) {
    val resolver = context.contentResolver ?: return
    var cursor: Cursor? = null
    try {
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null
        ) ?: return

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
            val fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            val tempUri = getContentUri(0, id) ?: return

            Log.d(
                TAG, """ 
            id == $id
            tempUri == $tempUri
            path == $path
            fileName == $fileName
        """.trimIndent()
            )
            val fileInput = getFileInput(context, tempUri)
            val realBitmap = BitmapFactory.decodeStream(fileInput)
            Log.d(TAG, "byte count  == ${realBitmap.byteCount}")
            imageView.setImageBitmap(realBitmap)
        }

    } catch (exception: Exception) {
        exception.printStackTrace()
    } finally {
        cursor?.close()
    }
}


/**
 *
 * @param type Int 0:图片 1：视频 2:音频
 * @param id Long 查询的ID
 * @return Uri?
 */
fun getContentUri(type: Int, id: Long): Uri? {
    return when (type) {
        0 -> {
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        }
        1 -> {
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        }
        2 -> {
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        }
        else -> null
    }
}

fun getFileInput(@NonNull context: Context, @NonNull uri: Uri): InputStream? {
    val resolver = context.contentResolver
    runCatching {
        return resolver.openInputStream(uri)
        val openAssetFileDescriptor = resolver.openAssetFileDescriptor(uri, "r") ?: return null
        val parcelFileDescriptor = openAssetFileDescriptor.parcelFileDescriptor
        if (parcelFileDescriptor != null) {
            return FileInputStream(parcelFileDescriptor.fileDescriptor)
        }
    }.onFailure {
        it.printStackTrace()
    }
    return null
}


fun insertImageIntoMediaStore(context: Context, fileName: String, mimeType: String, fileDir: String): Uri? {
    val contentValues = ContentValues()
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
    contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
    contentValues.put(
        MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}${File.separator}$fileDir"
    )
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

fun copyImageToPublicDir(@NonNull context: Context, @NonNull file: File) {
    if (file.exists() && file.isFile) {
        val uri = insertImageIntoMediaStore(
            context, file.name, "application/image", "ApplicationPic"
        ) ?: return
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


/**
 * uri 转 文件路径
 * @param uri Uri
 * @return String
 */
private fun uri2Path(@NonNull uri: Uri, @NonNull context: Context): String {
    // content 开头
    val schemeContent = ContentResolver.SCHEME_CONTENT
    // file 开头
    val schemeFile = ContentResolver.SCHEME_FILE
    return when (uri.scheme) {
        schemeContent -> {
            contentUri2Path(uri, context)
        }
        schemeFile -> {
            ""
        }

        else -> {
            ""
        }
    }
}

/**
 * content开头的uri转文件path
 * @return String
 */
private fun contentUri2Path(@NonNull uri: Uri, @NonNull context: Context): String {
    val documentUri = DocumentsContract.isDocumentUri(context, uri)
    if (documentUri) {
        when {
            isExternalStorageDocument(uri) -> {
                val documentId = DocumentsContract.getDocumentId(uri)
                Log.d(TAG, "external storage document documentId is $documentId")
                val split = documentId.split(":")
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return "${Environment.getExternalStorageDirectory()}${File.separator}${split[1]}"
                }
                return ""
            }

            isDownloadsDocument(uri) -> {
                val documentId = DocumentsContract.getDocumentId(uri)
                Log.d(TAG, "download document documentId is $documentId")
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), documentId.toLong()
                )
                return getDataColumn(context, contentUri, null, null)
            }

            isMediaDocument(uri) -> {
                val documentId = DocumentsContract.getDocumentId(uri)
                Log.d(TAG, "media documentId is $documentId")
                val split = documentId.split(":")
                val type = split[0]
                Log.d(TAG, "media type is $type")
                when (type) {
                    "document" -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val findDocumentPath =
                                DocumentsContract.findDocumentPath(context.contentResolver, uri) ?: return ""
                            findDocumentPath.path.forEach {
                                Log.d(TAG, "document path is i")
                            }
                        } else {

                        }
                        return ""
                    }
                }
            }

            else -> {
                return ""
            }
        }
    }

    return ""
}

private fun isExternalStorageDocument(@NonNull uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(@NonNull uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(@NonNull uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String {
    var cursor: Cursor? = null
    val column = "_data"
    val projections = arrayOf(column)
    kotlin.runCatching {
        cursor = context.contentResolver.query(uri, projections, selection, selectionArgs, null)
        if (cursor?.moveToFirst() == true) {
            val columnIndex = cursor?.getColumnIndexOrThrow(column) ?: return ""
            return cursor?.getString(columnIndex) ?: ""
        }
    }.getOrDefault {
        cursor?.close()
    }
    return ""
}



