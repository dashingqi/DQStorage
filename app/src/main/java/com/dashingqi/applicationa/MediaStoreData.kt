package com.dashingqi.applicationa

import android.net.Uri
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 封装从媒体文件中查询的数据
 * @author zhangqi61
 * @since 2022/10/18
 */
class MediaStoreData {
    /** uri */
    var uri: Uri? = null

    /** relative_path*/
    var relativePath: String = ""

    /** _ID */
    var _id: Long = -1L

    /** display name */
    var displayName: String = ""

    /** 被添加的时间*/
    var dateAdded: Long = -1L
    override fun toString(): String {
        return "MediaStoreData(uri=$uri, " +
                "relativePath='$relativePath', " +
                "_id=$_id, " +
                "displayName='$displayName', " +
                "dateAdded=$dateAdded" +
                "dateAddedFormat=${formatLongTime2Date(dateAdded)}" +
                ")"
    }

    /**
     * 时间格式化
     * @param addedTime Long 毫秒
     * @return String 时间戳
     */
    private fun formatLongTime2Date(addedTime: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(Date(addedTime))
    }

}