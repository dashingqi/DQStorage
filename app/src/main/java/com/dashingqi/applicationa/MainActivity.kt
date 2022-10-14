package com.dashingqi.applicationa

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.NonNull
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


        findViewById<Button>(R.id.btnPermission).setOnClickListener {
            // requestPermission()
            assetManager()
            scanMedia(this, iV)
        }

        findViewById<Button>(R.id.btnB).setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                // 指定类型
                type = "application/image"
                startActivityForResult(this, 10001)
            }
        }

        "s123123".toLongOrNull()


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


    /**
     * uri 转 文件路径
     * @param uri Uri
     * @return String
     */
    private fun uri2Path(@NonNull uri: Uri): String {
        // content 开头
        val schemeContent = ContentResolver.SCHEME_CONTENT
        // file 开头
        val schemeFile = ContentResolver.SCHEME_FILE
        return when (uri.scheme) {
            schemeContent -> {
                contentUri2Path(uri)
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
    private fun contentUri2Path(@NonNull uri: Uri): String {
        val documentUri = DocumentsContract.isDocumentUri(this, uri)
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
                    return getDataColumn(this, contentUri, null, null)
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
                                    DocumentsContract.findDocumentPath(contentResolver, uri) ?: return ""
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
        val filePath = filesDir.absolutePath + "/img.png"
        val file = File(filePath)
        if (file.isFile && file.exists()) {
            return file
        }
        return null
    }
}