package com.dashingqi.applicationa

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.NonNull
import androidx.annotation.Nullable

/**
 * 媒体文件、图片、视频扫描扩展类
 * @author zhangqi61
 * @since 2022/10/18
 */


/** MediaStore Tag */
internal const val MEDIA_STORE_TAG = "MediaStoreTag"

/**
 * 扫描媒体文件、图片、视频
 * @param context Context 上下文环境
 * @param scanType Int 扫描类别
 * @return MutableList<MediaStoreData>? 扫描结果集合
 */
fun scanMediaStoreFiles(
    @NonNull context: Context,
    @ScanMediaStoreType scanType: Int,
    @Nullable projection: (() -> Array<String>)? = null,
    @Nullable selection: (() -> String)? = null,
    @Nullable selectionArgs: (() -> Array<String>)? = null,
    @Nullable sortOrder: (() -> String)? = null
): MutableList<MediaStoreData>? {
    val projectArray = projection?.invoke()
    val selectionStr = selection?.invoke()
    val selectionArgsArray = selectionArgs?.invoke()
    val sortOrderStr = sortOrder?.invoke()
    return when (scanType) {
        ScanMediaStoreType.MS_PICTURE -> {
            scanPictureFiles(context, projectArray, selectionStr, selectionArgsArray, sortOrderStr)
        }
        ScanMediaStoreType.MS_AUDIO -> {
            scanAudioFiles(context, projectArray, selectionStr, selectionArgsArray, sortOrderStr)
        }
        ScanMediaStoreType.MS_VIDEO -> {
            scanVideoFiles(context, projectArray, selectionStr, selectionArgsArray, sortOrderStr)
        }
        else -> {
            null
        }
    }
}

/**
 * 扫描图片数据
 */
@Nullable
private fun scanPictureFiles(
    @NonNull context: Context,
    @Nullable projection: Array<String>?,
    @Nullable selection: String?,
    @Nullable selectionArgs: Array<String>?,
    @Nullable sortOrder: String?
):
        MutableList<MediaStoreData>? {
    // 这个是查询 在Pictures目录下存储的图片（如果拥有读写权限也是可以读取其他应用在此文件夹下的图片）
    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    ) ?: return null
    val picUriArray = mutableListOf<MediaStoreData>()
    while (cursor.moveToNext()) {
        val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
        val realPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
        } else {
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
        }
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        val picUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        val mediaStoreData = MediaStoreData()
        mediaStoreData._id = id
        mediaStoreData.uri = picUri
        mediaStoreData.relativePath = realPath
        mediaStoreData.displayName = displayName
        picUriArray.add(mediaStoreData)
    }

    return picUriArray
}

/**
 * 扫描媒体文件
 */
@Nullable
private fun scanAudioFiles(
    @NonNull context: Context,
    @Nullable projection: Array<String>?,
    @Nullable selection: String?,
    @Nullable selectionArgs: Array<String>?,
    @Nullable sortOrder: String?
): MutableList<MediaStoreData>? {
    val resolver = context.contentResolver
    val audioCursor = resolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    ) ?: return null
    val audioUriArray = mutableListOf<MediaStoreData>()
    while (audioCursor.moveToNext()) {
        val displayName = audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
        val realPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH))
        } else {
            audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
        }
        val id = audioCursor.getLong(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
        val audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
        val mediaStoreData = MediaStoreData()
        mediaStoreData._id = id
        mediaStoreData.uri = audioUri
        mediaStoreData.relativePath = realPath
        mediaStoreData.displayName = displayName
        audioUriArray.add(mediaStoreData)
    }
    return audioUriArray
}

/**
 * 扫描视频文件
 */
@Nullable
private fun scanVideoFiles(
    @NonNull context: Context,
    @Nullable projection: Array<String>?,
    @Nullable selection: String?,
    @Nullable selectionArgs: Array<String>?,
    @Nullable sortOrder: String?
): MutableList<MediaStoreData>? {
    val resolver = context.contentResolver
    val videoCursor = resolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    ) ?: return null
    val audioUriArray = mutableListOf<MediaStoreData>()
    while (videoCursor.moveToNext()) {
        val displayName = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
        val realPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH))
        } else {
            videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
        }
        val id = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
        val dateAdded = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
        val videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        val mediaStoreData = MediaStoreData()
        mediaStoreData._id = id
        mediaStoreData.uri = videoUri
        mediaStoreData.relativePath = realPath
        mediaStoreData.displayName = displayName
        mediaStoreData.dateAdded = dateAdded
        audioUriArray.add(mediaStoreData)
    }
    return audioUriArray
}
