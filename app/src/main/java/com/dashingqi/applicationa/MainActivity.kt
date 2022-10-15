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

        findViewById<Button>(R.id.btnB).setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                // 指定类型
                type = "application/image"
                startActivityForResult(this, 10001)
            }
        }
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
                this, Manifest.permission.MANAGE_DOCUMENTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.MANAGE_DOCUMENTS), 1000
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