package com.dashingqi.applicationa

import androidx.annotation.IntDef
import com.dashingqi.applicationa.ScanMediaStoreType.Companion.MS_AUDIO
import com.dashingqi.applicationa.ScanMediaStoreType.Companion.MS_PICTURE
import com.dashingqi.applicationa.ScanMediaStoreType.Companion.MS_VIDEO

/**
 * 扫描文件的类别
 * @author zhangqi61
 * @since 2022/10/18
 */
@IntDef(MS_PICTURE, MS_VIDEO, MS_AUDIO)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScanMediaStoreType {
    companion object {
        /** 扫描 picture */
        const val MS_PICTURE = 0

        /** 扫描 视频*/
        const val MS_VIDEO = 1

        /** 扫描媒体文件*/
        const val MS_AUDIO = 2
    }
}
