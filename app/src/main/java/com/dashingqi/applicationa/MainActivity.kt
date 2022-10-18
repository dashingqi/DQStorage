package com.dashingqi.applicationa

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*

/** storage tag */
const val TAG = "DQStorageTAG"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.requestPermission).setOnClickListener {
            // 请求权限
            requestPermission()
        }

        findViewById<Button>(R.id.btnA).setOnClickListener {
            val file = getFile() ?: return@setOnClickListener
            Log.d(TAG, "perform copy file")
            copyImageToPublicDir(this@MainActivity, file)
        }



        val iV = findViewById<ImageView>(R.id.bitmap)
        printPathName()


        findViewById<Button>(R.id.btnPermission).setOnClickListener {
            assetManager()

        }

        findViewById<Button>(R.id.btnC).setOnClickListener {
            scanImageMedia(this, iV)
        }

        /**
         * text/plain（纯文本）
        text/html（HTML文档）
        application/xhtml+xml（XHTML文档）
        image/gif（GIF图像）
        image/jpeg（JPEG图像）【PHP中为：image/pjpeg】
        image/png（PNG图像）【PHP中为：image/x-png】
        video/mpeg（MPEG动画）
        application/octet-stream（任意的二进制数据）
        application/pdf（PDF文档）
        application/msword（Microsoft Word文件）
        message/rfc822（RFC 822形式）
         */
        findViewById<Button>(R.id.btnB).setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                // 指定类型
                type = "image/*"
                startActivityForResult(this, 10001)
            }
        }

        val scanPicturesDir = scanMediaStoreFiles(this, ScanMediaStoreType.MS_PICTURE)
        Log.d(MEDIA_STORE_TAG, "$scanPicturesDir")
        Log.d(MEDIA_STORE_TAG, "===================================")
        val scanAudioDir = scanMediaStoreFiles(this, ScanMediaStoreType.MS_AUDIO)
        Log.d(MEDIA_STORE_TAG, "$scanAudioDir")
        Log.d(MEDIA_STORE_TAG, "===================================")
        val scanVideoList = scanMediaStoreFiles(this, ScanMediaStoreType.MS_VIDEO)
        Log.d(MEDIA_STORE_TAG, "$scanVideoList")
        Log.d(MEDIA_STORE_TAG, "===================================")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                // saf
                10001 -> {
                    kotlin.runCatching {
                        val dataUri = intent?.data ?: return
                        Log.d(TAG, "onActivityResult: dataUri is $dataUri")
                        val isFile = getFileInput(this, dataUri)
                        if (isFile != null) {
                            Log.d(TAG, "isFile is not null ")
                        } else {
                            Log.d(TAG, "isFile is null ")
                        }

                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000
            )
        }
    }

    private fun assetManager() {
        var iss: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            iss = assets.open("img.png")
            fos = openFileOutput("img.png", Context.MODE_PRIVATE)
            iss.copyTo(fos, 1024)
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            kotlin.runCatching {
                fos?.close()
            }
        }
    }

    /**
     * 读取内部存储中的文件
     * @return File?
     */
    private fun getFile(): File? {
        // /data/user/0/com.dashingqi.applicationa/files
        val absolutePath = filesDir.absolutePath
        val cacheDirPath = cacheDir.absolutePath
        Log.d(
            TAG, """
            cacheDirPath is $cacheDirPath
            absolutePath is $absolutePath
            
        """.trimIndent()
        )
        val filePath = filesDir.absolutePath + "/img.png"
        val file = File(filePath)
        if (file.isFile && file.exists()) {
            return file
        }
        return null
    }

    private fun printPathName() {
        // /data/user/0/com.dashingqi.applicationa/files
        val absolutePath = filesDir.absolutePath
        // /data/user/0/com.dashingqi.applicationa/cache
        val cacheDirPath = cacheDir.absolutePath
        val dataDirPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // /data/user/0/com.dashingqi.applicationa
            dataDir.absolutePath
        } else {
            ""
        }
        Log.d(
            TAG, """
            cacheDirPath is $cacheDirPath
            absolutePath is $absolutePath
            dataDirPath is $dataDirPath
        """.trimIndent()
        )
    }
}